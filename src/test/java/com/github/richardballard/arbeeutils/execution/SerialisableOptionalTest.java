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

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class SerialisableOptionalTest {

  public void fromOptionalHoldsOptional() {
    final String value = "value";
    final SerialisableOptional<String> so = SerialisableOptional.fromOptional(Optional.of(value));

    assertThat(so.asOptional())
        .contains(value);
  }

  public void emptyIsEmpty() {
    assertThat(SerialisableOptional.empty().asOptional())
        .isEmpty();
  }

  public void ofHoldsValue() {
    final String value = "value";
    final SerialisableOptional<String> so = SerialisableOptional.of(value);

    assertThat(so.asOptional())
        .contains(value);
  }

  public void ofNullableHandlesNull() {
    assertThat(SerialisableOptional.ofNullable(null).asOptional())
        .isEmpty();
  }

  public void ofNullableHandlesNonnull() {
    final String value = "value";

    assertThat(SerialisableOptional.ofNullable(value).asOptional())
        .contains(value);
  }

  @SuppressWarnings("unchecked")
  public void canBeSerialised() throws IOException, ClassNotFoundException {
    final SerialisableOptional<String> original = SerialisableOptional.of("hello");

    // serialise it
    final byte[] bytes;
    try(final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024)) {
      try(final ObjectOutput oos = new ObjectOutputStream(baos)) {
        oos.writeObject(original);
      }

      bytes = baos.toByteArray();
    }

    // deserialise it
    final SerialisableOptional<String> copy;
    try(final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      //noinspection CastToConcreteClass
      copy = (SerialisableOptional<String>)ois.readObject();
    }

    assertThat(copy)
        .isEqualTo(original);


  }

}
