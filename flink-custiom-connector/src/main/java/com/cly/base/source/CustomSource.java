package com.cly.base.source;


import org.apache.flink.api.connector.source.*;
import org.apache.flink.core.io.SimpleVersionedSerializer;

import java.io.IOException;

public class CustomSource implements Source {

    @Override
    public Boundedness getBoundedness() {
        return null;
    }

    @Override
    public SourceReader createReader(SourceReaderContext readerContext) {
        return null;
    }

    @Override
    public SplitEnumerator createEnumerator(SplitEnumeratorContext enumContext) {
        return null;
    }

    @Override
    public SplitEnumerator restoreEnumerator(SplitEnumeratorContext enumContext, Object checkpoint) throws IOException {
        return null;
    }

    @Override
    public SimpleVersionedSerializer getSplitSerializer() {
        return null;
    }

    @Override
    public SimpleVersionedSerializer getEnumeratorCheckpointSerializer() {
        return null;
    }
}
