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

package com.github.richardballard.arbeeutils.function;

import com.google.common.collect.ImmutableList;
import com.github.richardballard.arbeeutils.test.MockUtils;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Test
public class RefreshableMemoisingSupplierTest {

    @NotNull
    private Supplier<Integer> getDelegate() {
        return MockUtils.mockSupplierMultipleAnswers(ImmutableList.of(1, 2, 3, 4, 5, 6, 7, 8));
    }

    public void getReturnsMemoised() {
        final RefreshableMemoisingSupplier<Integer> supplier = new RefreshableMemoisingSupplier<>(getDelegate());

        assertThat(supplier.get())
                .isEqualTo(1);

        assertThat(supplier.get())
                .isEqualTo(1);
    }

    public void invalidateOnceAdvancesMemoised() {
        final RefreshableMemoisingSupplier<Integer> supplier = new RefreshableMemoisingSupplier<>(getDelegate());

        assertThat(supplier.get())
                .isEqualTo(1);

        supplier.invalidate();

        assertThat(supplier.get())
                .isEqualTo(2);
    }

    public void invalidateMultipleAdvancesMemoised() {
        final RefreshableMemoisingSupplier<Integer> supplier = new RefreshableMemoisingSupplier<>(getDelegate());

        assertThat(supplier.get())
                .isEqualTo(1);

        supplier.invalidate();
        supplier.invalidate();
        supplier.invalidate();

        assertThat(supplier.get())
                .isEqualTo(2);
    }

    public void invalidateDuringGetAdvancesMemoised() {
        final Supplier<Integer> delegate = getDelegate();

        final RefreshableMemoisingSupplier<Integer> supplier = new RefreshableMemoisingSupplier<>(delegate);

        final AtomicInteger count = new AtomicInteger(1);

        when(delegate.get())
                .thenAnswer(invocation -> {
                    supplier.invalidate();

                    return count.getAndIncrement();
                });

        assertThat(supplier.get())
                .isEqualTo(1);

        assertThat(supplier.get())
                .isEqualTo(2);
    }


}
