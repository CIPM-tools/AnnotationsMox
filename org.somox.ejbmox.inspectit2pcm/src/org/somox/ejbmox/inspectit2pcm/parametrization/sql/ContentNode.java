package org.somox.ejbmox.inspectit2pcm.parametrization.sql;

public class ContentNode<T> extends Node<T> {

	private T content;

	public ContentNode(T content) {
		this(content, 1);
	}

	// TODO needed?
	public ContentNode(T content, int count) {
		super(count);
		this.content = content;
	}

	public T getContent() {
		return content;
	}

	@Override
	public String toString() {
		return content.toString();
	}

}
