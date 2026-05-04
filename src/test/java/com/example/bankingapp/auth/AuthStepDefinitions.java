package com.example.bankingapp.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class AuthStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    private String username;
    private String password;
    private String latestToken;
    private int latestStatus;
    private AuthResponse latestAuthResponse;
    private ProfileResponse latestProfileResponse;

    @Given("a unique user with password {string}")
    public void aUniqueUserWithPassword(String password) {
        this.username = "functional-user-" + System.nanoTime();
        this.password = password;
    }

    @When("the user registers")
    public void theUserRegisters() {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/register",
                new AuthRequest(username, password),
                AuthResponse.class
        );
        latestStatus = response.getStatusCode().value();
        latestAuthResponse = response.getBody();
        latestProfileResponse = null;
        latestToken = latestAuthResponse.token();
    }

    @When("the user logs in")
    public void theUserLogsIn() {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/login",
                new AuthRequest(username, password),
                AuthResponse.class
        );
        latestStatus = response.getStatusCode().value();
        latestAuthResponse = response.getBody();
        latestProfileResponse = null;
        latestToken = latestAuthResponse.token();
    }

    @When("the user requests their profile")
    public void theUserRequestsTheirProfile() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(latestToken);
        ResponseEntity<ProfileResponse> response = restTemplate.exchange(
                "/api/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProfileResponse.class
        );
        latestStatus = response.getStatusCode().value();
        latestAuthResponse = null;
        latestProfileResponse = response.getBody();
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        assertThat(latestStatus).isEqualTo(status);
    }

    @Then("the response includes an auth token")
    public void theResponseIncludesAnAuthToken() {
        assertThat(latestAuthResponse.token()).isNotBlank();
    }

    @Then("the response includes the username")
    public void theResponseIncludesTheUsername() {
        String responseUsername = latestAuthResponse != null ? latestAuthResponse.username() : latestProfileResponse.username();
        assertThat(responseUsername).isEqualTo(username);
    }

    private record ProfileResponse(String username) {
    }
}
