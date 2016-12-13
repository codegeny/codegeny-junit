package org.codegeny.junit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class PresidentService {
	
	@Inject
	private PresidentRepository presidentRepository;
	
	@Transactional
	public void doSomething() {
		this.presidentRepository.addPresident(new President(3L, "Abraham Lincoln"));
		this.presidentRepository.findPresident(2L).ifPresent(this.presidentRepository::removePresident);
	}
}