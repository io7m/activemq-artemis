/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.artemis.protocol.amqp.sasl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * The default ServiceLoader-based SASL mechanism finder. The implementation will
 * search for implementations of the {@link ServerSASLFactory} interface using {@link ServiceLoader}.
 */

public final class SASLMechanismFinderDefault implements SASLMechanismFinder {

   private static final Map<String, ServerSASLFactory> FACTORY_MAP = new HashMap<>();
   private static final Comparator<? super ServerSASLFactory> PRECEDENCE_COMPARATOR =
      Comparator.comparingInt(ServerSASLFactory::getPrecedence);

   private static final String[] DEFAULT_MECHANISMS;
   private static String[] MECHANISMS;

   static {
      ServiceLoader<ServerSASLFactory> serviceLoader =
         ServiceLoader.load(ServerSASLFactory.class, SASLMechanismFinderDefault.class.getClassLoader());
      for (ServerSASLFactory factory : serviceLoader) {
         FACTORY_MAP.merge(factory.getMechanism(), factory, (f1, f2) -> {
            if (f2.getPrecedence() > f1.getPrecedence()) {
               return f2;
            } else {
               return f1;
            }
         });
      }
      DEFAULT_MECHANISMS =
         FACTORY_MAP
         .values()
         .stream()
         .filter(ServerSASLFactory::isDefaultPermitted)
         .sorted(PRECEDENCE_COMPARATOR.reversed())
         .map(ServerSASLFactory::getMechanism)
         .toArray(String[]::new);

      MECHANISMS = DEFAULT_MECHANISMS.clone();
   }

   @Override
   public String[] getDefaultSaslMechanisms() {
      return DEFAULT_MECHANISMS.clone();
   }

   @Override
   public String[] getSaslMechanisms() {
      return MECHANISMS.clone();
   }

   @Override
   public ServerSASLFactory getFactory(String mechanism) {
      return FACTORY_MAP.get(mechanism);
   }

   @Override
   public void setSaslMechanisms(String[] saslMechanisms) {
      final List<String> defaultMechanisms = List.of(DEFAULT_MECHANISMS);
      final List<String> newMechanisms = new ArrayList<>();

      for (final String mechanism : saslMechanisms) {
         if (defaultMechanisms.contains(mechanism)) {
            newMechanisms.add(mechanism);
         } else {
            throw new IllegalArgumentException(
               String.format(
                  "Mechanism '%s' does not appear in the default mechanism list (%s)",
                  mechanism,
                  defaultMechanisms
               )
            );
         }
      }

      MECHANISMS = newMechanisms.toArray(new String[0]);
   }
}
