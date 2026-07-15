package com.cardex.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:cardex-test",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"pokemon-tcg.base-url=http://localhost",
		"pokemon-tcg.api-key=test-key"
})
class CardexApiApplicationTests {

	@Test
	void contextLoads() {
	}
}