/*
 * (C) Copyright 2016 Richard Ballard.
 *
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
 */

package com.github.richardballard.arbeeutils.execution;

import com.google.common.base.Preconditions;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * NOTE - This code is taken almost verbatim from <a href="https://github.com/CodeFX-org/demo-serialize-optional">
 *   a CodeFx blog here</a>.  The original source has the <a href="http://unlicense.org">unlicensed licence</a>.  For a
 *   description of the motivation for this class see <a href="http://blog.codefx.org/java/serialize-optional/> the blog
 *   post itself</a>
 *
 * Convenience class to wrap an {@link Optional} for serialisation. Instances of this class are immutable.
 * <p>
 * Note that it does not provide any of the methods {@code Optional} has as its only goal is to enable serialisation.
 * But it holds a reference to the {@code Optional} which was used to create it (can be accessed with
 * {@link #asOptional()}). This {@code Optional} instance is of course reconstructed on deserialisation, so it will not
 * be the same as the one specified for its creation.
 * <p>
 * The class can be used as an argument or return type for serialisation-based RPC technologies like RMI.
 * <p>
 * There are three ways to use this class to serialise instances which have an optional field.
 * <p>
 * <h2>Transform For Serialisation Proxy</h2> If the class is serialised using the Serialisation Proxy Pattern (see
 * <i>Effective Java, 2nd Edition</i> by Joshua Bloch, Item 78), the proxy can have an instance of
 * {@link SerialisableOptional} to clearly denote the field as being optional.
 * <p>
 * In this case, the proxy needs to transform the {@code Optional} to {@code SerialisableOptional} in its constructor
 * (using {@link SerialisableOptional#fromOptional(Optional)}) and the other way in {@code readResolve()} (with
 * {@link SerialisableOptional#asOptional()}).
 * <p>
 * A code example can be found in this class which implements the pattern.
 * <p>
 * <h2>Transform For Custom Serialised Form</h2> The original field can be declared as
 * {@code transient Optional<T> optionalField}, which will exclude it from serialisation.
 * <p>
 * The class then needs to implement custom (de)serialisation methods {@code writeObject} and {@code readObject}. They
 * must transform the {@code optionalField} to a {@code SerialisableOptional} when writing the object and after reading
 * such an instance transform it back to an {@code Optional}.
 * <p>
 * <h3>Code Example</h3>
 *
 * <pre>
 * private void writeObject(ObjectOutputStream out) throws IOException {
 * 	out.defaultWriteObject();
 * 	out.writeObject(
 * 		SerialisableOptional.fromOptional(optionalField));
 * }
 *
 * private void readObject(ObjectInputStream in)
 * 	throws IOException, ClassNotFoundException {
 *
 * 	in.defaultReadObject();
 * 	optionalField =
 * 		((SerialisableOptional<T>) in.readObject()).toOptional();
 * }
 * </pre>
 * <p>
 * <h2>Transform For Access</h2> The field can be declared as {@code SerialisableOptional<T> optionalField}. This will
 * include it in the (de)serialisation process so it does not need to be customized.
 * <p>
 * But methods interacting with the field need to get an {@code Optional} instead. This can easily be done by writing
 * the accessor methods such that they transform the field on each access.
 * <p>
 * Note that {@link #asOptional()} simply returns the {@code Optional} which with this instance was created so no
 * constructor needs to be invoked.
 * <p>
 * <h3>Code Example</h3> Note that it is rarely useful to expose an optional field via accessor methods. Hence the
 * following are private and for use inside the class.
 *
 * <pre>
 * private Optional<T> getOptionalField() {
 * 	return optionalField.asOptional();
 * }
 *
 * private void setOptionalField(Optional<T> optionalField) {
 * 	this.optionalField = SerialisableOptional.fromOptional(optionalField);
 * }
 * </pre>
 *
 * @param <T>
 *            the type of the wrapped value
 */
@SuppressWarnings({"SerializableDeserializableClassInSecureContext", "WeakerAccess"})
@Immutable
public class SerialisableOptional <T extends Serializable> implements Serializable {

  // ATTRIBUTES

  private static final long serialVersionUID = -652697447004597911L;

  /**
   * The wrapped {@link Optional}. Note that this field is transient so it will not be (de)serialised automatically.
   */
  private final @NotNull Optional<T> optional;

  // CONSTRUCTION AND TRANSFORMATION

  private SerialisableOptional(final @NotNull Optional<T> optional) {
    Preconditions.checkNotNull(optional,
                               "The argument 'optional' must not be null.");

    this.optional = optional;
  }

  /**
   * Creates a serialisable optional from the specified optional.
   *
   * @param <T>
   *            the type of the wrapped value
   * @param optional
   *            the {@link Optional} from which the serialisable wrapper will be created
   * @return an instance which wraps the specified optional
   */
  public static @NotNull <T extends Serializable> SerialisableOptional<T> fromOptional(
      final @NotNull Optional<T> optional) {

    return new SerialisableOptional<>(optional);
  }

  /**
   * Creates a serialisable optional which wraps an empty optional.
   *
   * @param <T>
   *            the type of the non-existent value
   * @return an instance which wraps an {@link Optional#empty() empty} {@link Optional}
   * @see Optional#of(Object)
   */
  public static @NotNull <T extends Serializable> SerialisableOptional<T> empty() {
    return new SerialisableOptional<>(Optional.empty());
  }

  /**
   * Creates a serialisable optional for the specified, non-null value by wrapping it in an {@link Optional}.
   *
   * @param <T>
   *            the type of the wrapped value
   * @param value
   *            the value which will be contained in the wrapped {@link Optional}; must not be null
   * @return an instance which wraps the an optional for the specified value
   * @throws NullPointerException
   *             if value is null
   * @see Optional#of(Object)
   */
  @SuppressWarnings("ProhibitedExceptionDeclared")
  public static @NotNull <T extends Serializable> SerialisableOptional<T> of(final @NotNull T value)
      throws NullPointerException {

    Objects.requireNonNull(value);

    return new SerialisableOptional<>(Optional.of(value));
  }

  /**
   * Creates a serialisable optional for the specified, possibly null value by wrapping it in an {@link Optional}.
   *
   * @param <T>
   *            the type of the wrapped value
   * @param value
   *            the value which will be contained in the wrapped {@link Optional}; may be null
   * @return an instance which wraps the an optional for the specified value
   * @see Optional#ofNullable(Object)
   */
  public static @NotNull <T extends Serializable> SerialisableOptional<T> ofNullable(final @Nullable T value) {
    return new SerialisableOptional<>(Optional.ofNullable(value));
  }

  /**
   * Returns the {@code Optional} instance with which this instance was created.
   *
   * @return this instance as an {@link Optional}
   */
  public @NotNull Optional<T> asOptional() {
    return optional;
  }

  @Override
  public boolean equals(final Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }

    final SerialisableOptional<?> that = (SerialisableOptional<?>) o;

    return optional.equals(that.optional);
  }

  @Override
  public int hashCode() {
    return optional.hashCode();
  }

  @Override
  public String toString() {
    return "SerialisableOptional{" +
           "optional=" + optional +
           '}';
  }

  // SERIALIZATION

  protected @NotNull Object writeReplace() {
    return new SerialisationProxy<>(this);
  }

  @SuppressWarnings("OverlyBroadThrowsClause")
  private void readObject(final @NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {
    throw new InvalidObjectException("Serialisation proxy expected.");
  }

  @Immutable
  private static class SerialisationProxy<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -1326520485869949065L;

    private final @Nullable T value;

    public SerialisationProxy(final @NotNull SerialisableOptional<T> serialisableOptional) {

      value = serialisableOptional.asOptional().orElse(null);
    }

    private @NotNull Object readResolve() {
      return ofNullable(value);
    }

  }

}

