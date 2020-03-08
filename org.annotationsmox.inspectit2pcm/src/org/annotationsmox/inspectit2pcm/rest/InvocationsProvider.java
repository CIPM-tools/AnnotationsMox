package org.annotationsmox.inspectit2pcm.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.annotationsmox.inspectit2pcm.model.InvocationSequence;

public class InvocationsProvider implements Iterable<InvocationSequence> {

    private final InvocationsServiceClient invocationsService;

    private final List<Long> invocationIds;

    private boolean detailed = false;

    private InvocationsProvider(List<Long> invocationIds, InvocationsServiceClient invocationsService) {
        this.invocationIds = invocationIds;
        this.invocationsService = invocationsService;

    }

    /**
     * Removes invocations considered to belong to the warmup phase and returns the result as a new
     * {@link InvocationsProvider}. The called instance remains unchanged.
     * 
     * @param count
     *            the warmup phase length (number of measururements)
     */
    public InvocationsProvider removeWarmup(int count) {
        List<Long> invocationIdsWithoutWarmup = removeWarmupInvocations(count);
        return new InvocationsProvider(invocationIdsWithoutWarmup, invocationsService);
    }

    /**
     * 
     * @param ids
     *            the invocation ids to be removed
     * @return
     */
    public InvocationsProvider remove(Collection<Long> ids) {
        List<Long> result = new ArrayList<>(invocationIds);
        result.removeAll(ids);
        return new InvocationsProvider(result, invocationsService);
    }

    @Override
    public Iterator<InvocationSequence> iterator() {
        return new InvocationSequenceIterator();
    }

    public int size() {
        return invocationIds.size();
    }

    private List<Long> removeWarmupInvocations(int warmupLength) {
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

    public void setDetailed(boolean detailed) {
        this.detailed = detailed;
    }

    public boolean isDetailed() {
        return detailed;
    }

    public static InvocationsProvider fromService(InvocationsServiceClient invocationsService) {
        List<Long> invocationIds = invocationsService.getInvocationSequencesId();
        InvocationsProvider provider = new InvocationsProvider(invocationIds, invocationsService);
        return provider;
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
            InvocationSequence invocationSequence = invocationsService.getInvocationSequence(invocationId, detailed);
            return invocationSequence;
        }

    }

}
