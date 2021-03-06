package net.sf.dz3.view.http.common;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import net.sf.dz3.view.http.v1.HttpConnector;

import org.apache.log4j.NDC;

/**
 * Keeps sending data that appears in {@link HttpConnector#upstreamQueue} to the server
 * right away, and accepting whatever they have to say.
 *
 * @param <DataBlock> Data type to send out to the server.
 * 
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2001-2011
 */
public abstract class ImmediateExchanger<DataBlock> extends AbstractExchanger<DataBlock> {

    public ImmediateExchanger(URL serverContextRoot, String username, String password, BlockingQueue<DataBlock> upstreamQueue) {

        super(serverContextRoot, username, password, upstreamQueue);
    }

    /**
     * Keep sending data that appears in {@link HttpConnector#upstreamQueue} to the server
     * right away, and accepting whatever they have to say.
     */
    @Override
    protected void execute() throws Throwable {
        
        NDC.push("execute");
        
        try {

            while (isEnabled()) {
                
                try {
                    
                    exchange(upstreamQueue.take());

                } catch (Throwable t) {
                    
                    // Can't afford to bail out, this may be a transient condition
                    logger.error("Unexpected exception", t);
                }    
            }
            
        } finally {
            NDC.pop();
        }
    }
    
    /**
     * Exchange information with the server.
     * 
     * Information received from the server will be processed asynchronously.
     * 
     * @param dataBlock Block to send.
     * 
     * @throws IOException if things go sour.
     */
    protected final void exchange(DataBlock dataBlock) throws IOException {
        
        NDC.push("exchange");
        
        try {
            
            send(dataBlock);
            
            // VT: FIXME: Process the response
            
        } finally {
            NDC.pop();
        }
    }

    protected abstract void send(DataBlock dataBlock) throws IOException;
}
