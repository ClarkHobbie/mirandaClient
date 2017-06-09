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

package com.ltsllc.miranda.event;

import com.ltsllc.miranda.Message;
import com.ltsllc.miranda.event.messages.CreateEventMessage;
import com.ltsllc.miranda.event.messages.ReadEventMessage;
import com.ltsllc.miranda.manager.DirectoryManager;
import com.ltsllc.miranda.manager.ListMessage;
import com.ltsllc.miranda.reader.Reader;
import com.ltsllc.miranda.writer.Writer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Clark on 5/1/2017.
 */
public class EventManager extends DirectoryManager {
    public static final String NAME = "event manager";

    public EventManager (String directoryName, int objectLimit, Reader reader, Writer writer) throws IOException {
        super(NAME, directoryName, objectLimit, reader, writer);

        EventManagerReadyState eventManagerReadyState = new EventManagerReadyState(this);
        setCurrentState(eventManagerReadyState);
    }

    public void sendReadEventMessage (BlockingQueue<Message> senderQueue, Object sender, String guid) {
        ReadEventMessage readEventMessage = new ReadEventMessage(senderQueue, sender, guid);
        sendToMe(readEventMessage);
    }

    public void sendCreateEventMessage (BlockingQueue<Message> senderQueue, Object sender, Event event) {
        CreateEventMessage createEventMessage = new CreateEventMessage(senderQueue, sender, event);
        sendToMe(createEventMessage);
    }

    public void sendListMessage (BlockingQueue<Message> senderQueue, Object sender) {
        ListMessage listMessage = new ListMessage(senderQueue, sender);
        sendToMe(listMessage);
    }
}
