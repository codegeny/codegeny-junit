package org.codegeny.junit.database;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.openejb.injection.FallbackPropertyInjector;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.mockito.MockitoInjector;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.MockInjector;
import org.codegeny.junit.PresidentRepository;
import org.codegeny.junit.PresidentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(ApplicationComposer.class)
@Classes(cdi = true, value = PresidentService.class)
public class ApplicationComposerWithMock {
	
	@Mock 
	private PresidentRepository presidentRepository;
	
	@Inject
	private PresidentService presidentService;
		
	@Test
	public void testSomething() {
		doReturn(Optional.empty()).when(presidentRepository).findPresident(2L);
		
		this.presidentService.doSomething();
		
		verify(presidentRepository).addPresident(any());
		verify(presidentRepository).findPresident(2L);
	}

	@MockInjector
	public Class<? extends FallbackPropertyInjector> mockInjector() {
		return MockitoInjector.class;
	}
}
