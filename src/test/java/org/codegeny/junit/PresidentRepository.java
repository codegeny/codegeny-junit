package org.codegeny.junit;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class PresidentRepository {
	
	@PersistenceContext(unitName = "managed")
	private EntityManager entityManager;
	
	public Optional<President> findPresident(long id) {
		return Optional.ofNullable(this.entityManager.getReference(President.class, id));
	}
	
	public void removePresident(President president) {
		this.entityManager.remove(president);
	}
	
	public void addPresident(President president) {
		this.entityManager.persist(president);
	}
}