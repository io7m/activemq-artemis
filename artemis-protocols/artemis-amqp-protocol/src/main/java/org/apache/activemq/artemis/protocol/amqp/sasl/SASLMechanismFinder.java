package org.apache.activemq.artemis.protocol.amqp.sasl;

/**
 * A function that can be used to lookup SASL mechanisms.
 */

public interface SASLMechanismFinder {

   /**
    * @return The default available SASL mechanisms
    */

   String[] getDefaultSaslMechanisms();

   /**
    * @return The available SASL mechanisms
    * @see #setSaslMechanisms(String[])
    */

   String[] getSaslMechanisms();

   /**
    * Find a suitable SASL factory for a given SASL mechanism name. Returns {@code null} if no factory
    * exists for the given named mechanism.
    *
    * @param mechanism The SASL mechanism name (such as "PLAIN")
    * @return A SASL factory for the given mechanism name
    */

   ServerSASLFactory getFactory(String mechanism);

   /**
    * Set the exposed SASL mechanisms. This can be used to restrict the mechanisms exposed by
    * {@link #getSaslMechanisms()}.
    *
    * @param saslMechanisms The SASL mechanisms
    */

   void setSaslMechanisms(String[] saslMechanisms);
}
