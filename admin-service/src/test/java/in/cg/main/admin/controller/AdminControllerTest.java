package in.cg.main.admin.controller;

import in.cg.main.admin.entity.Report;
import in.cg.main.admin.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

	@Mock
	private AdminService service;
	@InjectMocks
	private AdminController controller;

	@Test
	void approveClaim_shouldReturnResult() {
		when(service.approveClaim(1L)).thenReturn(new Object());
		assertNotNull(controller.approveClaim(1L));
	}

	@Test
	void rejectClaim_shouldReturnResult() {
		when(service.rejectClaim(1L)).thenReturn(new Object());
		assertNotNull(controller.rejectClaim(1L));
	}

	@Test
	void generateReport_shouldReturnReport() {
		Report r = new Report();
		r.setContent("Report Content");
		when(service.generateReport()).thenReturn(r);
		assertEquals("Report Content", controller.generateReport().getContent());
	}

	@Test
	void downloadClaimDocument_shouldReturnBytes() {
		byte[] mockData = "PDF Content".getBytes();
		when(service.downloadClaimDocument(1L)).thenReturn(mockData);
		assertArrayEquals(mockData, controller.downloadClaimDocument(1L));
	}

	@Test
	void closeClaim_shouldReturnResult() {
		when(service.closeClaim(1L)).thenReturn(new Object());
		assertNotNull(controller.closeClaim(1L));
	}
}
