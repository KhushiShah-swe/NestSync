package com.nestsync;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.nestsync.repository.UserRepository;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;

  record Account(String token, long id, String invite) {}

  private Account register(String name) throws Exception {
    var response =
        read(
            mvc.perform(
                    post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            json.writeValueAsString(
                                Map.of(
                                    "name",
                                    name,
                                    "email",
                                    name + "@example.com",
                                    "password",
                                    "password123"))))
                .andExpect(status().isCreated()));
    var token = response.get("token").asText();
    var home =
        read(
            mvc.perform(get("/api/groups/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()));
    return new Account(
        token, response.get("user").get("userId").asLong(), home.get("inviteCode").asText());
  }

  private JsonNode read(ResultActions result) throws Exception {
    return json.readTree(result.andReturn().getResponse().getContentAsString());
  }

  private Map<String, Object> body(String resource) {
    return switch (resource) {
      case "expenses" ->
          new LinkedHashMap<>(
              Map.of(
                  "title",
                  "Internet",
                  "amount",
                  "10.00",
                  "date",
                  "2026-09-18",
                  "category",
                  "Utilities",
                  "splitType",
                  "EQUAL"));
      case "chores" ->
          new LinkedHashMap<>(
              Map.of(
                  "choreName",
                  "Clean kitchen",
                  "dueDate",
                  "2026-09-19",
                  "recurrence",
                  "WEEKLY",
                  "status",
                  "TODO"));
      case "groceries" ->
          new LinkedHashMap<>(Map.of("itemName", "Milk", "quantity", 2, "purchased", false));
      default ->
          new LinkedHashMap<>(
              Map.of(
                  "title", "Leaky tap", "description", "Kitchen faucet drips.", "status", "OPEN"));
    };
  }

  private String idField(String resource) {
    return switch (resource) {
      case "expenses" -> "expenseId";
      case "chores" -> "choreId";
      case "groceries" -> "groceryId";
      default -> "issueId";
    };
  }

  private JsonNode create(Account user, String resource, Map<String, Object> body)
      throws Exception {
    return read(
        mvc.perform(
                post("/api/" + resource)
                    .header("Authorization", "Bearer " + user.token())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(body)))
            .andExpect(status().isCreated()));
  }

  private void join(Account user, Account host) throws Exception {
    mvc.perform(
            post("/api/groups/join")
                .header("Authorization", "Bearer " + user.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("inviteCode", host.invite()))))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "expenses",
        "chores",
        "groceries",
        "maintenance",
        "balances",
        "groups/me",
        "users/me"
      })
  void requiresAuthentication(String path) throws Exception {
    mvc.perform(get("/api/" + path))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Sign in to continue."));
  }

  @Test
  void healthAndApiDocsAreAvailable() throws Exception {
    mvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/expenses']").exists());
  }

  @Test
  void registrationHashesPasswordAndHidesItFromResponses() throws Exception {
    var alice = register("Alice");
    var stored = users.findByEmail("alice@example.com").orElseThrow();
    assertThat(stored.getPassword()).isNotEqualTo("password123");
    assertThat(encoder.matches("password123", stored.getPassword())).isTrue();
    mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + alice.token()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Alice"))
        .andExpect(jsonPath("$.password").doesNotExist());
    mvc.perform(get("/api/groups/members").header("Authorization", "Bearer " + alice.token()))
        .andExpect(jsonPath("$[0].password").doesNotExist());
  }

  @Test
  void loginRejectsWrongPasswordAndAcceptsCaseNormalizedEmail() throws Exception {
    register("Alice");
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ALICE@example.com\",\"password\":\"password123\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isString());
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"password\":\"incorrect\"}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"unknown@example.com\",\"password\":\"incorrect\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void preventsDuplicateEmailAndInvalidRegistration() throws Exception {
    register("Alice");
    mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Other\",\"email\":\"ALICE@example.com\",\"password\":\"password123\"}"))
        .andExpect(status().isConflict());
    mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"email\":\"bad\",\"password\":\"short\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.email").exists());
  }

  @Test
  void rejectsPasswordsExceedingBcryptByteLimit() throws Exception {
    mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        Map.of(
                            "name",
                            "Unicode",
                            "email",
                            "unicode@example.com",
                            "password",
                            "界".repeat(30)))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void invalidBearerTokensNeverAuthenticate() throws Exception {
    mvc.perform(get("/api/expenses").header("Authorization", "Bearer invalid.token.value"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/expenses").header("Authorization", "Bearer "))
        .andExpect(status().isUnauthorized());
  }

  @ParameterizedTest
  @ValueSource(strings = {"expenses", "chores", "groceries", "maintenance"})
  void supportsValidatedCrudAndReturns404AfterDeletion(String resource) throws Exception {
    var alice = register("Alice");
    var payload = body(resource);
    var created = create(alice, resource, payload);
    long id = created.get(idField(resource)).asLong();
    mvc.perform(
            get("/api/" + resource + "/" + id).header("Authorization", "Bearer " + alice.token()))
        .andExpect(status().isOk());
    payload.put(
        resource.equals("chores")
            ? "choreName"
            : resource.equals("groceries") ? "itemName" : "title",
        "Updated record");
    mvc.perform(
            put("/api/" + resource + "/" + id)
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(payload)))
        .andExpect(status().isOk());
    mvc.perform(
            delete("/api/" + resource + "/" + id)
                .header("Authorization", "Bearer " + alice.token()))
        .andExpect(status().isNoContent());
    mvc.perform(
            get("/api/" + resource + "/" + id).header("Authorization", "Bearer " + alice.token()))
        .andExpect(status().isNotFound());
    mvc.perform(
            delete("/api/" + resource + "/" + id)
                .header("Authorization", "Bearer " + alice.token()))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(strings = {"expenses", "chores", "groceries", "maintenance"})
  void preventsCrossHouseholdReadsUpdatesAndDeletes(String resource) throws Exception {
    var alice = register("Alice");
    var outsider = register("Outside");
    var payload = body(resource);
    long id = create(alice, resource, payload).get(idField(resource)).asLong();
    mvc.perform(get("/api/" + resource).header("Authorization", "Bearer " + outsider.token()))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
    mvc.perform(
            get("/api/" + resource + "/" + id)
                .header("Authorization", "Bearer " + outsider.token()))
        .andExpect(status().isNotFound());
    mvc.perform(
            put("/api/" + resource + "/" + id)
                .header("Authorization", "Bearer " + outsider.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(payload)))
        .andExpect(status().isNotFound());
    mvc.perform(
            delete("/api/" + resource + "/" + id)
                .header("Authorization", "Bearer " + outsider.token()))
        .andExpect(status().isNotFound());
    mvc.perform(
            get("/api/" + resource + "/" + id).header("Authorization", "Bearer " + alice.token()))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"expenses", "chores", "groceries", "maintenance"})
  void rejectsMissingFields(String resource) throws Exception {
    var alice = register("Alice");
    mvc.perform(
            post("/api/" + resource)
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").isMap());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-1", "1.001", "10000000000"})
  void rejectsInvalidExpenseAmounts(String amount) throws Exception {
    var alice = register("Alice");
    var payload = body("expenses");
    payload.put("amount", amount);
    mvc.perform(
            post("/api/expenses")
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(payload)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rejectsUnsupportedSplitTypes() throws Exception {
    var alice = register("Alice");
    var payload = body("expenses");
    payload.put("splitType", "PERCENTAGE");
    mvc.perform(
            post("/api/expenses")
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(payload)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void splitsExactlyAndPreservesOriginalParticipantsOnEdit() throws Exception {
    var alice = register("Alice");
    var bob = register("Bob");
    var cara = register("Cara");
    join(bob, alice);
    join(cara, alice);
    var payload = body("expenses");
    payload.put("paidBy", Map.of("userId", bob.id()));
    payload.put("shares", Map.of("999", 0));
    var expense = create(alice, "expenses", payload);
    assertThat(expense.get("paidBy").get("userId").asLong()).isEqualTo(alice.id());
    assertThat(expense.get("shares").get(Long.toString(alice.id())).decimalValue())
        .isEqualByComparingTo("3.34");
    var balances =
        read(
            mvc.perform(get("/api/balances").header("Authorization", "Bearer " + bob.token()))
                .andExpect(status().isOk()));
    BigDecimal total = BigDecimal.ZERO;
    for (var balance : balances) total = total.add(balance.get("netAmount").decimalValue());
    assertThat(total).isEqualByComparingTo("0.00");
    assertThat(balances.get(0).get("netAmount").decimalValue()).isEqualByComparingTo("6.66");
    var dave = register("Dave");
    join(dave, alice);
    payload.put("amount", "12.00");
    mvc.perform(
            put("/api/expenses/" + expense.get("expenseId").asLong())
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shares['" + dave.id() + "']").doesNotExist())
        .andExpect(jsonPath("$.shares['" + bob.id() + "']").value(4.0));
  }

  @Test
  void choreAssigneesMustBelongToTheSameHousehold() throws Exception {
    var alice = register("Alice");
    var bob = register("Bob");
    var payload = body("chores");
    payload.put("assigneeId", bob.id());
    mvc.perform(
            post("/api/chores")
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(payload)))
        .andExpect(status().isBadRequest());
    join(bob, alice);
    var chore = create(alice, "chores", payload);
    assertThat(chore.get("assignee").get("userId").asLong()).isEqualTo(bob.id());
    assertThat(chore.get("assignee").has("password")).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"expenses", "chores", "groceries", "maintenance"})
  void cannotAbandonAHouseholdWithRecords(String resource) throws Exception {
    var alice = register("Alice");
    var bob = register("Bob");
    create(alice, resource, body(resource));
    mvc.perform(
            post("/api/groups/join")
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("inviteCode", bob.invite()))))
        .andExpect(status().isConflict());
  }

  @Test
  void joiningIsIdempotentAndUnknownInvitesReturn404() throws Exception {
    var alice = register("Alice");
    join(alice, alice);
    mvc.perform(
            post("/api/groups/join")
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"inviteCode\":\"unknown\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void cannotLeaveOtherHouseholdMembersBehind() throws Exception {
    var alice = register("Alice");
    var bob = register("Bob");
    var cara = register("Cara");
    join(bob, alice);
    mvc.perform(
            post("/api/groups/join")
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("inviteCode", cara.invite()))))
        .andExpect(status().isConflict());
  }

  @Test
  void malformedJsonReturnsProblemDetails() throws Exception {
    var alice = register("Alice");
    mvc.perform(
            post("/api/expenses")
                .header("Authorization", "Bearer " + alice.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{broken"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").isString());
  }

  @Test
  void corsAllowsConfiguredFrontendAndRejectsOtherOrigins() throws Exception {
    mvc.perform(
            options("/api/expenses")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    mvc.perform(
            options("/api/expenses")
                .header("Origin", "https://untrusted.example")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isForbidden());
  }
}
