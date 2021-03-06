/*
 * Copyright 2017 Long Term Software LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ltsllc.miranda.servlet.login;

import com.ltsllc.miranda.EncryptedMessage;
import com.ltsllc.miranda.servlet.objects.ResultObject;

/**
 * Created by Clark on 4/15/2017.
 */
public class LoginResultObject extends ResultObject {
    private String session;
    private String category;
    private EncryptedMessage encryptedMessage;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public EncryptedMessage getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setEncryptedMessage(EncryptedMessage encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }
}
