package com.rocket.api.setup.annotations;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/*
 * JUnit extension that cleans up rocket-related tables before each test.
 * Use with @CleanDBState annotation on test classes.
 */
public class CleanDBStateExtension implements BeforeEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) {
    var applicationContext = SpringExtension.getApplicationContext(context);
    var jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
    cleanupCatalogTables(jdbcTemplate);
  }

  public static void cleanupCatalogTables(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.update("DELETE FROM rocket_event");
    jdbcTemplate.update("DELETE FROM rocket_state");
  }
}