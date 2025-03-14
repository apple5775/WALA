/*
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package com.ibm.wala.util.graph.traverse;

import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.util.debug.UnimplementedError;
import com.ibm.wala.util.graph.Graph;
import com.uber.nullaway.annotations.Initializer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

/**
 * This class implements depth-first search over a {@link Graph}, return an enumeration of the nodes
 * of the graph in order of increasing finishing time. This class follows the outNodes of the graph
 * nodes to define the graph, but this behavior can be changed by overriding the getConnected
 * method.
 */
public abstract class DFSFinishTimeIterator<T> extends ArrayList<T> implements Iterator<T> {

  private static final long serialVersionUID = 8440061593631309429L;

  /** the current next element in finishing time order */
  private @Nullable T theNextElement;

  /** an enumeration of all nodes to search from */
  private Iterator<? extends T> roots;

  /** The governing graph. */
  private Graph<T> G;

  /** Subclasses must call this in the constructor! */
  @Initializer
  protected void init(Graph<T> G, Iterator<? extends T> nodes) {
    this.G = G;
    roots = nodes;
    if (roots.hasNext()) theNextElement = roots.next();
  }

  private boolean empty() {
    return size() == 0;
  }

  /**
   * Return whether there are any more nodes left to enumerate.
   *
   * @return true if there nodes left to enumerate.
   */
  @Override
  public boolean hasNext() {
    return (!empty() || (theNextElement != null && getPendingChildren(theNextElement) == null));
  }

  abstract @Nullable Iterator<T> getPendingChildren(@Nullable T n);

  abstract void setPendingChildren(@Nullable T v, Iterator<T> iterator);

  private void push(@Nullable T elt) {
    add(elt);
  }

  private T peek() {
    return get(size() - 1);
  }

  private T pop() {
    T e = get(size() - 1);
    remove(size() - 1);
    return e;
  }

  /**
   * Find the next graph node in finishing time order.
   *
   * @return the next graph node in finishing time order.
   */
  @Override
  @SuppressWarnings("unchecked")
  public @Nullable T next() throws NoSuchElementException {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    if (empty()) {
      T v = theNextElement;
      setPendingChildren(v, getConnected(v));
      push(v);
    }
    recurse:
    while (!empty()) {
      T v = peek();
      Iterator<? extends T> pc = getPendingChildren(v);
      for (T n : Iterator2Iterable.make(pc)) {
        assert n != null : "null node in pc";
        Iterator<T> nChildren = getPendingChildren(n);
        if (nChildren == null) {
          // found a new child: recurse to it.
          setPendingChildren(n, getConnected(n));
          push(n);
          continue recurse;
        }
      }
      // the following saves space by allowing the original iterator to be GCed
      setPendingChildren(v, (Iterator<T>) EmptyIterator.instance());

      // no more children to visit: finished this vertex
      while (getPendingChildren(theNextElement) != null && roots.hasNext()) {
        theNextElement = roots.next();
      }

      return pop();
    }
    return null;
  }

  /**
   * get the out edges of a given node
   *
   * @param n the node of which to get the out edges
   * @return the out edges
   */
  protected Iterator<T> getConnected(@Nullable T n) {
    return G.getSuccNodes(n);
  }

  @Override
  public void remove() throws UnimplementedError {
    throw new UnimplementedError();
  }
}
