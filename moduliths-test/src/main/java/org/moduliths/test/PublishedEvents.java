/*
 * Copyright 2019-2020 the original author or authors.
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
package org.moduliths.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import org.springframework.util.Assert;

/**
 * All Spring application events fired during the test execution.
 *
 * @author Oliver Drotbohm
 */
public interface PublishedEvents {

	/**
	 * Creates a new {@link PublishedEvents} instance for the given events.
	 *
	 * @param events must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static PublishedEvents of(Object... events) {
		return of(Arrays.asList(events));
	}

	/**
	 * Creates a new {@link PublishedEvents} instance for the given events.
	 *
	 * @param events must not be {@literal null}.
	 * @return
	 */
	public static PublishedEvents of(Collection<? extends Object> events) {

		Assert.notNull(events, "Events must not be null!");

		return new DefaultPublishedEvents(events);
	}

	/**
	 * Returns all application events of the given type that were fired during the test execution.
	 *
	 * @param <T> the event type
	 * @param type must not be {@literal null}.
	 * @return
	 */
	<T> TypedPublishedEvents<T> ofType(Class<T> type);

	/**
	 * All application events of a given type that were fired during a test execution.
	 *
	 * @author Oliver Drotbohm
	 * @param <T> the event type
	 */
	interface TypedPublishedEvents<T> extends Iterable<T> {

		/**
		 * Further constrain the event type for downstream assertions.
		 *
		 * @param <S>
		 * @param subType the sub type
		 * @return
		 */
		<S extends T> TypedPublishedEvents<S> ofSubType(Class<S> subType);

		/**
		 * Returns all {@link TypedPublishedEvents} that match the given predicate.
		 *
		 * @param predicate must not be {@literal null}.
		 * @return
		 */
		TypedPublishedEvents<T> matching(Predicate<? super T> predicate);
	}
}
