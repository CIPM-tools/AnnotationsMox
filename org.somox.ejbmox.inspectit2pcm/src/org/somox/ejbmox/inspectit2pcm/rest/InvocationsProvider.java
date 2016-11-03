package org.somox.ejbmox.inspectit2pcm.rest;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;

public class InvocationsProvider implements Iterable<InvocationSequence> {

    private final InvocationsServiceClient invocationsService;

    private List<Long> invocationIds;

    private InvocationsProvider(InvocationsServiceClient invocationsService) {
        this.invocationsService = invocationsService;
        invocationIds = invocationsService.getInvocationSequencesId();
    }

    /**
     * Removes invocations considered to belong to the warmup phase
     * 
     * @param count
     *            the warmup phase length (number of measururements)
     */
    public void removeWarmup(int count) {
        invocationIds = removeWarmupInvocations(count, invocationIds);
    }

    @Override
    public Iterator<InvocationSequence> iterator() {
        return new InvocationSequenceIterator();
    }

    public int size() {
        return invocationIds.size();
    }

    private static List<Long> removeWarmupInvocations(int warmupLength, final List<Long> invocationIds) {
        int fromIndex = warmupLength;
        int toIndex = invocationIds.size(); // no "-1" because toIndex parameter is exclusive
        final List<Long> invocationIdsWithoutWarmup;
        if (fromIndex < toIndex) {
            invocationIdsWithoutWarmup = invocationIds.subList(fromIndex, toIndex);
        } else {
            invocationIdsWithoutWarmup = Collections.emptyList();
        }
        return invocationIdsWithoutWarmup;
    }

    public static InvocationsProvider fromService(InvocationsServiceClient invocationsService) {
        return new InvocationsProvider(invocationsService);
    }

    private class InvocationSequenceIterator implements Iterator<InvocationSequence> {
    
        private Iterator<Long> idIterator;
    
        public InvocationSequenceIterator() {
            this.idIterator = invocationIds.iterator();
        }
    
        @Override
        public boolean hasNext() {
            return idIterator.hasNext();
        }
    
        @Override
        public InvocationSequence next() {
            long invocationId = idIterator.next();
            InvocationSequence invocationSequence = invocationsService.getInvocationSequence(invocationId);
            return invocationSequence;
        }
    
    }

}
