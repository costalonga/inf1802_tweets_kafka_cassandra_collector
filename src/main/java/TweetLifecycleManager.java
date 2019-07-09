import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;


// Utilizando o Serializer com String

public class TweetLifecycleManager implements LifecycleManager {

    static String _consumerKey = System.getenv().get("TWITTER_CONSUMER_KEY");
    static String _consumerSecret = System.getenv().get("TWITTER_CONSUMER_SECRET");
    static String _accessToken = System.getenv().get("TWITTER_ACCESS_TOKEN");
    static String _accessTokenSecret = System.getenv().get("TWITTER_ACCESS_TOKEN_SECRET");

    static KafkaProducer<String,String> producer = getProducerInstance();


    public static KafkaProducer<String,String> getProducerInstance(){
        // Criar as propriedades do produtor
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // TODO SERIALIZER
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TweetSerializer.class.getName()); //FIXME

        // Criar o produtor
        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
        return producer;
    }

    public static TwitterStream getTwitterStreamInstance(){
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
//        configurationBuilder.setDebugEnabled(true);
        configurationBuilder.setOAuthConsumerKey(_consumerKey);
        configurationBuilder.setOAuthConsumerSecret(_consumerSecret);
        configurationBuilder.setOAuthAccessToken(_accessToken);
        configurationBuilder.setOAuthAccessTokenSecret(_accessTokenSecret);
        TwitterStreamFactory tf = new TwitterStreamFactory(configurationBuilder.build());
        return tf.getInstance();
    }

    StatusListener listener = new StatusListener() {
        @Override
        public void onStatus(Status status) {
            Tweet tweet = new Tweet(status);

            String message = new String();
            message= tweet.getCreatedDate() +
                    "-------" + tweet.getId() +
                    "-------" + tweet.getText() +
                    "-------" + tweet.getSource() +
                    "-------" + tweet.getIsTruncated() +
                    "-------" + tweet.getLatitude() +
                    "-------" + tweet.getLongitude() +
                    "-------" + tweet.getIsFavorited() +
                    "-------" + tweet.getUserName() +
                    "-------" + tweet.getLanguage() + "\n";

            // Enviar as mensagens
            ProducerRecord<String, String> record = new ProducerRecord<String, String>("tweets_input", message);
            producer.send(record); // Envio assíncrono

        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
        @Override
        public void onTrackLimitationNotice(int i) {}
        @Override
        public void onScrubGeo(long l, long l1) {}
        @Override
        public void onStallWarning(StallWarning stallWarning) {}
        @Override
        public void onException(Exception e) {
            e.printStackTrace();
        }
    };

    TwitterStream twitterStream = getTwitterStreamInstance();


    public void start(){
        if (producer == null) {
            producer = getProducerInstance();
        }
        twitterStream.addListener(listener);
        twitterStream.filter("basquete", "basketball", "NBA", "playoffs");
    }

    public void stop(){
        // Close Producer
        producer.close();
        producer = null;
        twitterStream.shutdown();
    }
}



// Utilizando o Serializer com Tweet
/*
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;

public class TweetLifecycleManager implements LifecycleManager {

    static String _consumerKey = System.getenv().get("TWITTER_CONSUMER_KEY");
    static String _consumerSecret = System.getenv().get("TWITTER_CONSUMER_SECRET");
    static String _accessToken = System.getenv().get("TWITTER_ACCESS_TOKEN");
    static String _accessTokenSecret = System.getenv().get("TWITTER_ACCESS_TOKEN_SECRET");

    static KafkaProducer<String,Tweet> producer = getProducerInstance();


    public static KafkaProducer<String,Tweet> getProducerInstance(){
        // Criar as propriedades do produtor
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // TODO SERIALIZER
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TweetSerializer.class.getName()); //FIXME


        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TweetSerializer.class.getName());



        // Criar o produtor
        KafkaProducer<String,Tweet> producer = new KafkaProducer<String, Tweet>(properties);
        return producer;
    }

    public static TwitterStream getTwitterStreamInstance(){
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
//        configurationBuilder.setDebugEnabled(true);
        configurationBuilder.setOAuthConsumerKey(_consumerKey);
        configurationBuilder.setOAuthConsumerSecret(_consumerSecret);
        configurationBuilder.setOAuthAccessToken(_accessToken);
        configurationBuilder.setOAuthAccessTokenSecret(_accessTokenSecret);
        TwitterStreamFactory tf = new TwitterStreamFactory(configurationBuilder.build());
        return tf.getInstance();
    }

    StatusListener listener = new StatusListener() {
        @Override
        public void onStatus(Status status) {
            Tweet tweet = new Tweet(status);

//            String message = new String();
//            message = "CreatedAt: " + tweet.getCreatedDate() +
//                    "\nId: " + tweet.getId() +
//                    "\nText: " + tweet.getText() +
//                    "\nSource: " + tweet.getSource() +
//                    "\nIsTruncated: " + tweet.getIsTruncated() +
//                    "\nLatitude: " + tweet.getLatitude() +
//                    "\nLongitude: " + tweet.getLongitude() +
//                    "\nIsFavorited: " + tweet.getIsFavorited() +
//                    "\nUser Name: " + tweet.getUserName() +
//                      "\nLanguage: " + tweet.getLanguage() + "\n";


            // Enviar as mensagens
//            ProducerRecord<String, String> record = new ProducerRecord<String, String>("tweets_input", message);
            ProducerRecord<String, Tweet> record = new ProducerRecord<String, Tweet>("tweets_input", tweet);
            producer.send(record); // Envio assíncrono

        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
        @Override
        public void onTrackLimitationNotice(int i) {}
        @Override
        public void onScrubGeo(long l, long l1) {}
        @Override
        public void onStallWarning(StallWarning stallWarning) {}
        @Override
        public void onException(Exception e) {
            e.printStackTrace();
        }
    };

    TwitterStream twitterStream = getTwitterStreamInstance();


    public void start(){
        if (producer == null) {
            producer = getProducerInstance();
        }
        twitterStream.addListener(listener);
        twitterStream.filter("basquete", "basketball", "NBA", "playoffs");
    }

    public void stop(){
        // Close Producer
        producer.close();
        producer = null;
        twitterStream.shutdown();
    }
}

*/