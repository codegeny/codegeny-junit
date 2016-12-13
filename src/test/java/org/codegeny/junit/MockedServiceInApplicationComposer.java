package org.codegeny.junit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.openejb.injection.FallbackPropertyInjector;
import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.mockito.MockitoInjector;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.MockInjector;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

@Classes(cdi = true, value = PresidentService.class)
public class MockedServiceInApplicationComposer {
	
	@Captor
	private ArgumentCaptor<President> captor;
	
	@Mock
	private PresidentRepository presidentRepository;
	
	@Rule
	public final TestRule ruleChain = new ApplicationComposerRule(this);

	@Inject
	private PresidentService service;
		
	@MockInjector
	public FallbackPropertyInjector mockInjector() {
		return new MockitoInjector();
	}
	
	@Test
	public void testSomething() {
		President franklingRoosevelt = new President(2L, "Franklin Roosevelt");
		doReturn(Optional.of(franklingRoosevelt)).when(this.presidentRepository).findPresident(2L);
		
		this.service.doSomething();
		
		verify(this.presidentRepository).addPresident(captor.capture());
		verify(this.presidentRepository).findPresident(2L);
		verify(this.presidentRepository).removePresident(argThat(is(franklingRoosevelt)));
		
		President abrahamLincoln = captor.getValue();
		assertThat(abrahamLincoln, notNullValue());
		assertThat(abrahamLincoln.getId(), is(3L));
		assertThat(abrahamLincoln.getName(), is("Abraham Lincoln"));
	}
}
