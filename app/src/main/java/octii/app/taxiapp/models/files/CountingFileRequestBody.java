/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 16.08.2021, 21:59                       *
 ******************************************************************************/

package octii.app.taxiapp.models.files;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class CountingFileRequestBody extends RequestBody {
    private static final String TAG = "CountingFileRequestBody";

    private final ProgressListener listener;
    private final String key;
    private final RequestBody multipartBody;
    protected CountingSink mCountingSink;

    public CountingFileRequestBody(RequestBody multipartBody,
                                   String key,
                                   ProgressListener listener) {
        this.multipartBody = multipartBody;
        this.listener = listener;
        this.key = key;
    }

    @Override
    public long contentLength() throws IOException {
        return multipartBody.contentLength();
    }

    @Override
    public MediaType contentType() {
        return multipartBody.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        mCountingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(mCountingSink);
        multipartBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    public interface ProgressListener {
        void transferred(String key, int num);
    }

    protected final class CountingSink extends ForwardingSink {
        private long bytesWritten = 0;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            bytesWritten += byteCount;
            listener.transferred(key, (int) (100F * bytesWritten / contentLength()));
            super.write(source, byteCount);
            delegate().flush(); // I have added this line to manually flush the sink
        }
    }

}
