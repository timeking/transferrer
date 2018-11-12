package com.github.timeking.transferrer;

import com.github.timeking.transferrer.dto.AccountDto;
import com.github.timeking.transferrer.dto.TransferDto;
import com.github.timeking.transferrer.dto.TransferState;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class AccountITs {

    private final RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(8080)
            .setBasePath("/api")
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();


    @Test
    public void givenNoAccount_whenCreate_thenReturnItBack() {
        int balance = 10;
        String accountId = createAccount(balance);

        given(requestSpec)
                .pathParam("id", accountId)
            .when()
                .get(EndPoints.ACCOUNTS)
            .then()
                .body("balance", Matchers.equalTo(balance))
                .body("accountId", Matchers.equalTo(accountId))
                .statusCode(Matchers.equalTo(HttpStatus.OK_200));
    }

    @Test
    public void givenIllegalAccountId_whenGetAccount_thenBadRequest() {
        ValidatableResponse response = given(requestSpec)
                .pathParam("id", "1234123")
        .when()
                .get(EndPoints.ACCOUNTS)
        .then()
                .statusCode(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void givenRandomUuidAccountId_whenGetAccount_thenNotFound() {
        ValidatableResponse response = given(requestSpec)
                .pathParam("id", UUID.randomUUID())
        .when()
                .get(EndPoints.ACCOUNTS)
        .then()
                .statusCode(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void givenAccount_whenDelete_thenNotFound() {
        String accountId = createAccount(10);

        given(requestSpec)
                .pathParam("id", accountId)
            .when()
                .delete(EndPoints.ACCOUNTS)
            .then()
                .statusCode(Matchers.equalTo(HttpStatus.NO_CONTENT_204));

        given(requestSpec)
                .pathParam("id", accountId)
            .when()
                .get(EndPoints.ACCOUNTS)
            .then()
                .statusCode(Matchers.equalTo(HttpStatus.NOT_FOUND_404));
    }

    private String createAccount(int balance) {
        ValidatableResponse response = given(requestSpec)
                .pathParam("id", "")
            .when()
                .body(AccountDto.builder().balance(balance).build())
                .post(EndPoints.ACCOUNTS)
            .then()
                .body("balance", Matchers.equalTo(balance))
                .body("accountId", Matchers.notNullValue())
                .statusCode(Matchers.equalTo(HttpStatus.CREATED_201));
        return response.extract().path("accountId");
    }


    @Test
    public void givenTwoAccountsAndTransferAmount_whenCreateTransfer_thenShouldCreate() {
        UUID accountFrom = UUID.fromString(createAccount(10));
        UUID accountTo = UUID.fromString(createAccount(100));
        int amount = 10;

        String transferId = createTransfer(accountFrom, accountTo, amount);
    }

    @Test
    public void givenTransfer_whenCreateTransfer_thenTransferExecuted() {
        UUID accountFrom = UUID.fromString(createAccount(10));
        UUID accountTo = UUID.fromString(createAccount(100));
        int amount = 10;

        String transferId = createTransfer(accountFrom, accountTo, amount);

        given(requestSpec)
                .pathParam("id", transferId)
            .when()
                .get(EndPoints.TRANSFERS)
            .then()
                .body("amount", Matchers.equalTo(amount))
                .body("accountFrom", Matchers.equalTo(accountFrom.toString()))
                .body("accountTo", Matchers.equalTo(accountTo.toString()))
                .body("date", Matchers.notNullValue())
                .body("state", Matchers.equalTo(
                        TransferState.TRANSFERRED.toString()
                ))
                .statusCode(HttpStatus.OK_200);

        checkAccountBalance(accountFrom, 0);
        checkAccountBalance(accountTo, 110);
    }

    @Test
    public void givenIllegalTransferAmount_whenCreateTransfer_thenTransferFail() {
        UUID accountFrom = UUID.fromString(createAccount(10));
        UUID accountTo = UUID.fromString(createAccount(100));
        int amount = 100;

        String transferId = createTransfer(accountFrom, accountTo, amount);

        given(requestSpec)
                .pathParam("id", transferId)
            .when()
                .get(EndPoints.TRANSFERS)
            .then()
                .body("amount", Matchers.equalTo(amount))
                .body("accountFrom", Matchers.equalTo(accountFrom.toString()))
                .body("accountTo", Matchers.equalTo(accountTo.toString()))
                .body("date", Matchers.notNullValue())
                .body("state", Matchers.equalTo(
                        TransferState.FAILED.toString()
                ))
                .statusCode(HttpStatus.OK_200);

        checkAccountBalance(accountFrom, 10);
        checkAccountBalance(accountTo, 100);
    }

    private void checkAccountBalance(UUID accountId, Integer expectedBalance) {
        ValidatableResponse response = given(requestSpec)
                .pathParam("id", accountId)
            .when()
                .get(EndPoints.ACCOUNTS)
            .then()
                .body("accountId", Matchers.equalTo(accountId.toString()))
                .body("balance", Matchers.equalTo(expectedBalance))
                .statusCode(Matchers.equalTo(HttpStatus.OK_200));
    }

    private String createTransfer(UUID accountFrom, UUID accountTo, int amount) {
        ValidatableResponse response = given(requestSpec)
                .pathParam("id", "")
            .when()
                .body(TransferDto.builder()
                        .accountFrom(accountFrom)
                        .accountTo(accountTo)
                        .amount(amount)
                        .build()
                )
                .post(EndPoints.TRANSFERS)
            .then()
                .body("amount", Matchers.equalTo(amount))
                .body("accountFrom", Matchers.equalTo(accountFrom.toString()))
                .body("accountTo", Matchers.equalTo(accountTo.toString()))
                .body("date", Matchers.notNullValue())
                .body("state", Matchers.notNullValue())
                .statusCode(HttpStatus.CREATED_201);
        return response.extract().path("transferId");
    }

    private static class EndPoints {
        public static final String ACCOUNTS = "/accounts/{id}";
        public static final String TRANSFERS = "/transfers/{id}";
    }
}
