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

package org.arbee.arbeeutils.test;

import org.jetbrains.annotations.NotNull;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * This class is based on code from http://rolf-engelhard.de/2011/10/fail-instead-of-skip-a-test-when-testngs-dataprovider-throws-an-exception/
 * <p/>
 * It is referenced as a listener from pom.xml - specifically the surefire config
 */
public class FailOnSkipTestListenerAdapter extends TestListenerAdapter {

    @Override
    public void onTestSkipped(@NotNull final ITestResult tr) {
        assert tr != null;

        tr.setStatus(ITestResult.FAILURE);
    }
}