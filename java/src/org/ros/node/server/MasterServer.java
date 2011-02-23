/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.node.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.ros.service.ServiceIdentifier;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.node.xmlrpc.MasterImpl;
import org.ros.topic.PublisherIdentifier;
import org.ros.topic.SubscriberIdentifier;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterServer extends NodeServer {

  private final Map<String, SlaveIdentifier> slaves;
  private final Map<String, ServiceIdentifier> services;
  private final Multimap<String, PublisherIdentifier> publishers;
  private final Multimap<String, SubscriberIdentifier> subscribers;

  public MasterServer(String hostname, int port) {
    super(hostname, port);
    slaves = Maps.newConcurrentMap();
    services = Maps.newConcurrentMap();
    publishers =
        Multimaps.synchronizedMultimap(ArrayListMultimap.<String, PublisherIdentifier>create());
    subscribers =
        Multimaps.synchronizedMultimap(ArrayListMultimap.<String, SubscriberIdentifier>create());
  }

  public void start() throws XmlRpcException, IOException {
    super.start(org.ros.node.xmlrpc.MasterImpl.class, new MasterImpl(this));
  }

  public void registerService(ServiceIdentifier description) {
    services.put(description.getName(), description);
  }

  public List<Object> unregisterService(String callerId, String service, String serviceApi) {
    return null;
  }

  private void addSlave(SlaveIdentifier description) {
    String name = description.getName();
    Preconditions.checkState(slaves.get(name) == null || slaves.get(name).equals(description));
    slaves.put(name, description);
  }

  /**
   * Subscribe the caller to the specified topic. In addition to receiving a
   * list of current publishers, the subscriber will also receive notifications
   * of new publishers via the publisherUpdate API.
   * 
   * @param description
   * 
   * 
   * @return Publishers is a list of XMLRPC API URIs for nodes currently
   *         publishing the specified topic.
   */
  public List<PublisherIdentifier> registerSubscriber(SubscriberIdentifier description) {
    subscribers.put(description.getTopicName(), description);
    addSlave(description.getSlaveIdentifier());
    return ImmutableList.copyOf(publishers.get(description.getTopicName()));
  }

  public List<Object> unregisterSubscriber(String callerId, String topic, String callerApi) {
    return null;
  }

  /**
   * Register the caller as a publisher the topic.
   * 
   * @param callerId ROS caller ID
   * @return List of current subscribers of topic in the form of XML-RPC URIs.
   */
  public List<SubscriberIdentifier> registerPublisher(String callerId,
      PublisherIdentifier description) {
    publishers.put(description.getTopicName(), description);
    addSlave(description.getSlaveIdentifier());
    return ImmutableList.copyOf(subscribers.get(description.getTopicName()));
  }

  public List<Object> unregisterPublisher(String callerId, String topic, String callerApi) {
    return null;
  }

  /**
   * Get the XML-RPC URI of the node with the associated name/caller_id. This
   * API is for looking information about publishers and subscribers. Use
   * lookupService instead to lookup ROS-RPC URIs.
   * 
   * @param callerId ROS caller ID
   * @param nodeName Name of node to lookup
   * @return
   */
  public SlaveIdentifier lookupNode(String callerId, String nodeName) {
    return slaves.get(nodeName);
  }

  public List<PublisherIdentifier> getPublishedTopics(String callerId, String subgraph) {
    // TODO(damonkohler): Add support for subgraph filtering.
    Preconditions.checkArgument(subgraph.length() == 0);
    return ImmutableList.copyOf(publishers.values());
  }

  public List<Object> getSystemState(String callerId) {
    return null;
  }

  public List<Object> getUri(String callerId) {
    return null;
  }

  /**
   * Lookup the provider of a particular service.
   * 
   * @param callerId ROS caller ID
   * @param service Fully-qualified name of service
   * @return service URI that provides address and port of the service. Fails if
   *         there is no provider.
   */
  public ServiceIdentifier lookupService(String callerId, String service) {
    return services.get(service);
  }
}