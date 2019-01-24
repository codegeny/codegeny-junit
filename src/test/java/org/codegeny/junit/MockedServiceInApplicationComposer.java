package org.codegeny.junit;

/*-
 * #%L
 * A collection of JUnit rules
 * %%
 * Copyright (C) 2016 - 2019 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
