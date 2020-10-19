/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.moduliths.model;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.persistence.Entity;

import org.jddd.core.types.AggregateRoot;
import org.jddd.core.types.Identifier;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.moduliths.model.ArchitecturallyEvidentType.SpringAwareArchitecturallyEvidentType;
import org.moduliths.model.ArchitecturallyEvidentType.SpringDataAwareArchitecturallyEvidentType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tngtech.archunit.core.domain.JavaClass;

/**
 * Unit tests for {@link ArchitecturallyEvidentType}.
 *
 * @author Oliver Drotbohm
 */
class ArchitecturallyEvidentTypeUnitTest {

	Classes classes = TestUtils.getClasses();
	JavaClass self = classes.getRequiredClass(ArchitecturallyEvidentTypeUnitTest.class);

	@Test // #101
	void abbreviatesFullyQualifiedTypeName() {

		ArchitecturallyEvidentType type = ArchitecturallyEvidentType.of(self, classes);

		assertThat(type.getAbbreviatedFullName()).isEqualTo("o.m.m.ArchitecturallyEvidentTypeUnitTest");
	}

	@Test // #101
	void doesNotConsiderArbitraryTypeAStereotype() {

		ArchitecturallyEvidentType type = ArchitecturallyEvidentType.of(self, classes);

		assertThat(type.isEntity()).isFalse();
		assertThat(type.isAggregateRoot()).isFalse();
		assertThat(type.isRepository()).isFalse();
	}

	@Test // #101
	void detectsSpringAnnotatedRepositories() {

		ArchitecturallyEvidentType type = new SpringAwareArchitecturallyEvidentType(
				classes.getRequiredClass(SpringRepository.class));

		assertThat(type.isRepository()).isTrue();
	}

	@Test // #101
	void doesNotConsiderEntityAggregateRoot() {

		ArchitecturallyEvidentType type = new SpringAwareArchitecturallyEvidentType(
				classes.getRequiredClass(SampleEntity.class));

		assertThat(type.isEntity()).isTrue();
		assertThat(type.isAggregateRoot()).isFalse();
	}

	@Test // #101
	void considersEntityAnAggregateRootIfTheresARepositoryForIt() {

		Map<Class<?>, Boolean> parameters = new HashMap<Class<?>, Boolean>();
		parameters.put(SampleEntity.class, true);
		parameters.put(OtherEntity.class, false);
		parameters.put(NoEntity.class, false);

		parameters.entrySet().stream().forEach(it -> {

			JavaClass entity = classes.getRequiredClass(it.getKey());

			assertThat(new SpringDataAwareArchitecturallyEvidentType(entity, classes).isAggregateRoot())
					.isEqualTo(it.getValue());
		});
	}

	@TestFactory // #104
	Stream<DynamicTest> considersJDddEntity() {

		return DynamicTest.stream(getTypesFor(JDddAnnotatedEntity.class, JDddImplementingEntity.class), //
				it -> String.format("%s is considered an entity", it.getType().getSimpleName()), //
				it -> {
					assertThat(it.isEntity()).isTrue();
					assertThat(it.isAggregateRoot()).isFalse();
					assertThat(it.isRepository()).isFalse();
				});
	}

	@TestFactory // #104
	Stream<DynamicTest> considersJDddAggregateRoot() {

		return DynamicTest.stream(getTypesFor(JDddAnnotatedAggregateRoot.class, JDddImplementingAggregateRoot.class), //
				it -> String.format("%s is considered an entity, aggregate root but not a repository",
						it.getType().getSimpleName()), //
				it -> {
					assertThat(it.isEntity()).isTrue();
					assertThat(it.isAggregateRoot()).isTrue();
					assertThat(it.isRepository()).isFalse();
				});
	}

	@TestFactory // #104
	Stream<DynamicTest> considersJDddRepository() {

		return DynamicTest.stream(getTypesFor(JDddAnnotatedRepository.class), //
				it -> String.format("%s is considered a repository", it.getType().getSimpleName()), //
				it -> {
					assertThat(it.isEntity()).isFalse();
					assertThat(it.isAggregateRoot()).isFalse();
					assertThat(it.isRepository()).isTrue();
				});
	}

	@TestFactory // #127
	Stream<DynamicTest> considersJMoleculesEntity() {

		return DynamicTest.stream(getTypesFor(JMoleculesAnnotatedEntity.class, JMoleculesImplementingEntity.class), //
				it -> String.format("%s is considered an entity", it.getType().getSimpleName()), //
				it -> {
					assertThat(it.isEntity()).isTrue();
					assertThat(it.isAggregateRoot()).isFalse();
					assertThat(it.isRepository()).isFalse();
				});
	}

	@TestFactory // #127
	Stream<DynamicTest> considersJMoleculesAggregateRoot() {

		return DynamicTest.stream(
				getTypesFor(JMoleculesAnnotatedAggregateRoot.class, JMoleculesImplementingAggregateRoot.class), //
				it -> String.format("%s is considered an entity, aggregate root but not a repository",
						it.getType().getSimpleName()), //
				it -> {
					assertThat(it.isEntity()).isTrue();
					assertThat(it.isAggregateRoot()).isTrue();
					assertThat(it.isRepository()).isFalse();
				});
	}

	@TestFactory // #127
	Stream<DynamicTest> considersJMoleculesRepository() {

		return DynamicTest.stream(getTypesFor(JMoleculesAnnotatedRepository.class), //
				it -> String.format("%s is considered a repository", it.getType().getSimpleName()), //
				it -> {
					assertThat(it.isEntity()).isFalse();
					assertThat(it.isAggregateRoot()).isFalse();
					assertThat(it.isRepository()).isTrue();
				});
	}

	private Iterator<ArchitecturallyEvidentType> getTypesFor(Class<?>... types) {

		return Stream.of(types) //
				.map(classes::getRequiredClass) //
				.map(it -> ArchitecturallyEvidentType.of(it, classes)) //
				.iterator();
	}

	// Spring

	@Repository
	interface SpringRepository {}

	@Entity
	class SampleEntity {}

	// Spring Data

	interface SampleRepository extends CrudRepository<SampleEntity, UUID> {}

	@Entity
	class OtherEntity {}

	class NoEntity {}

	// jDDD

	@org.jddd.core.annotation.Entity
	class JDddAnnotatedEntity {}

	@org.jddd.core.annotation.AggregateRoot
	class JDddAnnotatedAggregateRoot {}

	class JDddImplementingIdentifier implements Identifier {}

	abstract class JDddImplementingEntity
			implements org.jddd.core.types.Entity<JDddImplementingAggregateRoot, JDddImplementingIdentifier> {}

	abstract class JDddImplementingAggregateRoot
			implements AggregateRoot<JDddImplementingAggregateRoot, JDddImplementingIdentifier> {}

	@org.jddd.core.annotation.Repository
	class JDddAnnotatedRepository {}

	// jMolecules

	@org.jmolecules.ddd.annotation.Entity
	class JMoleculesAnnotatedEntity {}

	@org.jmolecules.ddd.annotation.AggregateRoot
	class JMoleculesAnnotatedAggregateRoot {}

	class JMoleculesImplementingIdentifier implements org.jmolecules.ddd.types.Identifier {}

	abstract class JMoleculesImplementingEntity
			implements
			org.jmolecules.ddd.types.Entity<JMoleculesImplementingAggregateRoot, JMoleculesImplementingIdentifier> {}

	abstract class JMoleculesImplementingAggregateRoot
			implements
			org.jmolecules.ddd.types.AggregateRoot<JMoleculesImplementingAggregateRoot, JMoleculesImplementingIdentifier> {}

	@org.jmolecules.ddd.annotation.Repository
	class JMoleculesAnnotatedRepository {}
}
