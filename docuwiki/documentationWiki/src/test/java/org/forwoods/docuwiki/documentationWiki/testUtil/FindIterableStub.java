package org.forwoods.docuwiki.documentationWiki.testUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bson.BsonArray;
import org.bson.Document;
import org.bson.codecs.BsonArrayCodec;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.json.JsonReader;
import org.forwoods.docuwiki.documentationWiki.resources.ClassResourceTest;

import com.mongodb.Block;
import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;

public class FindIterableStub<TResult> implements FindIterable<TResult> {

	
	

	Iterable<TResult> data;
	
	public FindIterableStub(Iterable<TResult> data) {
		this.data=data;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static  FindIterableStub<Document> createFromJson(String json) {
		final CodecRegistry codecRegistry = 
				CodecRegistries.fromProviders(new ValueCodecProvider(),
			    new BsonValueCodecProvider(),
			    new DocumentCodecProvider());
		
		JsonReader reader = new JsonReader(json);
		BsonArrayCodec arrayReader = new BsonArrayCodec(codecRegistry);
		BsonArray docArray = arrayReader.decode(reader, DecoderContext.builder().build());
		List<Document> data = docArray.getValues().stream()
				.map(bson->Document.parse(bson.toString()))
				.collect(Collectors.toList());
		return new FindIterableStub(data);
	}
	
	public static FindIterableStub<Document> loadResources(String... names) {
		List<Document> docs = new ArrayList<>();
		for (String name:names){
			InputStream stream = ClassResourceTest.class.getClassLoader()
					.getResourceAsStream(name) ;
			String text = new Scanner(stream).useDelimiter("\\A").next();
			Document d = Document.parse(text);
			docs.add(d);
		}
		return new FindIterableStub<>(docs);
	}
	
	@Override
	public MongoCursor<TResult> iterator() {
		return new MongoCursor<TResult>() {
			Iterator<TResult> under = data.iterator();
			@Override
			public void close() {
			}

			@Override
			public boolean hasNext() {
				return under.hasNext();
			}

			@Override
			public TResult next() {
				return under.next();
			}

			@Override
			public TResult tryNext() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ServerCursor getServerCursor() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ServerAddress getServerAddress() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public TResult first() {
		return data.iterator().next();
	}

	@Override
	public <R> MongoIterable<R> map(Function<TResult, R> mapper) {
		Iterable<R> result = new MappingIterable<R>(mapper);
		return new FindIterableStub<R>(result);
	}

	@Override
	public void forEach(Block<? super TResult> block) {
		for (TResult res:data) {
			block.apply(res);
		}
	}

	@Override
	public <A extends Collection<? super TResult>> A into(A target) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> filter(Bson filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> limit(int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> skip(int skip) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> maxTime(long maxTime, TimeUnit timeUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> maxAwaitTime(long maxAwaitTime, TimeUnit timeUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> modifiers(Bson modifiers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> projection(Bson projection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> sort(Bson sort) {
		return this;//TODO - thiss will be HARD!
	}

	@Override
	public FindIterable<TResult> noCursorTimeout(boolean noCursorTimeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> oplogReplay(boolean oplogReplay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> partial(boolean partial) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> cursorType(CursorType cursorType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FindIterable<TResult> batchSize(int batchSize) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class MappingIterable<R> implements Iterable<R> {
		
		private Function<TResult, R> mapper;

		private MappingIterable(Function<TResult, R> mapper){
			this.mapper = mapper;
		}
		
		@Override
		public Iterator<R> iterator() {
			return new Iterator<R>(){
				Iterator<TResult> underlying = data.iterator();

				@Override
				public boolean hasNext() {
					return underlying.hasNext();
				}

				@Override
				public R next() {
					return mapper.apply(underlying.next());
				}
			};
		};
	}

}
