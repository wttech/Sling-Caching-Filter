package com.cognifide.cq.cache.plugins.statistics.entry;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.lang.management.ManagementFactory;
import java.util.List;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticEntry {

	private static final Logger logger = LoggerFactory.getLogger(StatisticEntry.class);

	private final String cacheName;

	private final List<AttributeHolder<? extends Number>> attributes;

	private StatisticEntry(String cacheName, List<AttributeHolder<? extends Number>> attributes) {
		this.cacheName = cacheName;
		this.attributes = attributes;
	}

	public List<String> findHeaders() {
		List<String> headers = Lists.newArrayList();
		for (AttributeHolder<? extends Number> attribute : attributes) {
			headers.add(toHumanReadable(attribute.getName()));
		}
		return headers;
	}

	private String toHumanReadable(String s) {
		return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(s), ' ');
	}

	public List<Number> findValues() {
		List<Number> values = Lists.newArrayList();
		for (AttributeHolder<? extends Number> attribute : attributes) {
			values.add(attribute.getValue());
		}
		return values;
	}

	public String getCacheName() {
		return cacheName;
	}

	public static class Builder {

		private static final String CACHE_HITS = "CacheHits";

		private static final String CACHE_HIT_PERCENTAGE = "CacheHitPercentage";

		private static final String CACHE_MISSES = "CacheMisses";

		private static final String CACHE_MISS_PERCENTAGE = "CacheMissPercentage";

		private static final String CACHE_GETS = "CacheGets";

		private static final String CACHE_PUTS = "CachePuts";

		private static final String CACHE_REMOVALS = "CacheRemovals";

		private static final String CACHE_EVICTIONS = "CacheEvictions";

		private static final String AVERAGE_GET_TIME = "AverageGetTime";

		private static final String AVERAGE_PUT_TIME = "AveragePutTime";

		private static final String AVERAGE_REMOVE_TIME = "AverageRemoveTime";

		private final List<AttributeHolder<? extends Number>> attributes;

		private final CacheHolder cacheHolder;

		private final String cacheName;

		public Builder(CacheHolder cacheHolder, String cacheName) {
			this.cacheHolder = Preconditions.checkNotNull(cacheHolder);
			this.cacheName = Preconditions.checkNotNull(cacheName);

			attributes = Lists.newArrayList();
			attributes.add(new AttributeHolder<Long>(CACHE_HITS, Long.class));
			attributes.add(new AttributeHolder<Float>(CACHE_HIT_PERCENTAGE, Float.class));
			attributes.add(new AttributeHolder<Long>(CACHE_MISSES, Long.class));
			attributes.add(new AttributeHolder<Float>(CACHE_MISS_PERCENTAGE, Float.class));
			attributes.add(new AttributeHolder<Long>(CACHE_GETS, Long.class));
			attributes.add(new AttributeHolder<Long>(CACHE_PUTS, Long.class));
			attributes.add(new AttributeHolder<Long>(CACHE_REMOVALS, Long.class));
			attributes.add(new AttributeHolder<Long>(CACHE_EVICTIONS, Long.class));
			attributes.add(new AttributeHolder<Float>(AVERAGE_GET_TIME, Float.class));
			attributes.add(new AttributeHolder<Float>(AVERAGE_PUT_TIME, Float.class));
			attributes.add(new AttributeHolder<Float>(AVERAGE_REMOVE_TIME, Float.class));
		}

		public StatisticEntry build() {
			StatisticEntry statisticEntry = null;
			try {
				MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

				ObjectName objectName = new CacheObjectNameBuilder().buildObjectName(cacheHolder, cacheName);

				for (AttributeHolder<? extends Number> attribute : attributes) {
					attribute.compute(mBeanServer, objectName);
				}

				statisticEntry = new StatisticEntry(cacheName, attributes);
			} catch (AttributeNotFoundException x) {
				logger.error("Error while gathering statistics", x);
			} catch (MalformedObjectNameException x) {
				logger.error("Error while gathering statistics", x);
			} catch (MBeanException x) {
				logger.error("Error while gathering statistics", x);
			} catch (InstanceNotFoundException x) {
				logger.error("Error while gathering statistics", x);
			} catch (ReflectionException x) {
				logger.error("Error while gathering statistics", x);
			}

			return statisticEntry;
		}
	}

	private static class AttributeHolder<T> {

		private final String name;

		private final Class<T> clazz;

		private T value;

		private AttributeHolder(String name, Class<T> clazz) {
			this.name = name;
			this.clazz = clazz;
		}

		private void compute(MBeanServer mBeanServer, ObjectName objectName)
				throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
			value = clazz.cast(mBeanServer.getAttribute(objectName, name));
		}

		public String getName() {
			return name;
		}

		public T getValue() {
			return value;
		}

	}

}
