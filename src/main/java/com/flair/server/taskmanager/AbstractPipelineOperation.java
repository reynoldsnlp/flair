/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.taskmanager;

/**
 * Represents the interface of a (possibly executing) pipeline operation. A Future essentially.
 * @author shadeMe
 */	
public interface AbstractPipelineOperation
{
    /** 
     * Returns the type of pipeline operation
     * @return PipelineOperationType, either search crawl parse or custon operation
     */
    public PipelineOperationType	getType();
    
    /**
     * Initiates pipeline operation
     */
    public void						begin();
    /**
     * Returns true if operation has been canceled, false otherwise
     * @return Boolean value corresponding to cancellation of operation
     */
    public boolean					isCancelled();
    /**
     * Cancels pipeline operation
     */
    public void						cancel();
    /**
     * Returns true if operation has ended, (includes both completion and cancellation). Returns false if operation is still going
     * @return Boolean value corresponding to completetion of operation 
     */
    public boolean					isCompleted();	    // also returns true if the operation was cancelled
    /**
     * Waits for operation to finish
     */
    public void						waitForCompletion();
}
