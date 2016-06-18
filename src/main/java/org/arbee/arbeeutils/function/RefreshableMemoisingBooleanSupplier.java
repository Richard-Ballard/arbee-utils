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

package org.arbee.arbeeutils.function;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * This is equivalent to {@link RefreshableMemoisingSupplier} but implements {@link BooleanSupplier} rather than
 * {@link Supplier}.
 */
@ThreadSafe
public class RefreshableMemoisingBooleanSupplier implements BooleanSupplier {

    @NotNull
    private final RefreshableMemoisingSupplier<Boolean> coreSupplier;

    public RefreshableMemoisingBooleanSupplier(@NotNull final BooleanSupplier delegate) {
        assert delegate != null;

        this.coreSupplier = new RefreshableMemoisingSupplier<>(delegate::getAsBoolean);
    }

    /**
     * See {@link RefreshableMemoisingSupplier#get()}
     */
    @Override
    public boolean getAsBoolean() {
        return coreSupplier.get();
    }

    /**
     * See {@link RefreshableMemoisingSupplier#invalidate()}
     */
    public void invalidate() {
        coreSupplier.invalidate();
    }

    /**
     * See {@link RefreshableMemoisingSupplier#refresh()}
     */
    public boolean refresh() {
        return coreSupplier.refresh();
    }
}
