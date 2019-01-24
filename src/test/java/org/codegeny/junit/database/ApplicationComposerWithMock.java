package org.codegeny.junit.database;

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
