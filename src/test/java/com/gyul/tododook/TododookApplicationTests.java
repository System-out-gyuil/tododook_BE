package com.gyul.tododook;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest

@ActiveProfiles("Test") // 빌드할때 테스트시에만 작동하도록 명시
class TododookApplicationTests {

	@Test
	void contextLoads() {
	}

}
