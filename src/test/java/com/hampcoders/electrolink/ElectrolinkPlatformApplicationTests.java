package com.hampcoders.electrolink;

import io.karatelabs.core.Runner;
import io.karatelabs.core.SuiteResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ElectrolinkPlatformApplicationTests {

	@Test
	void contextLoads() {
		SuiteResult result = Runner.path("classpath:com/hampcoders/electrolink")
				.outputHtmlReport(true)
				.parallel(5);

		assertTrue(result.isPassed(), "Existen pruebas fallidas Karate");
	}

}