package com.cognifide.cq.cache.refresh.jcr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CacheEntryEventListener;
import com.opensymphony.oscache.base.events.CacheGroupEvent;
import com.opensymphony.oscache.base.events.CachePatternEvent;
import com.opensymphony.oscache.base.events.CachewideEvent;
import com.opensymphony.oscache.web.filter.ExpiresRefreshPolicy;

/**
 * @author Bartosz Rudnicki
 */
public abstract class JcrRefreshPolicy extends ExpiresRefreshPolicy implements JcrEventListener,
		CacheEntryEventListener {

	private static final long serialVersionUID = -8200859162145262469L;

	protected final List<Pattern> invalidatePaths = new ArrayList<Pattern>();

	private final String cacheEntryKey;

	private JcrEventsService jcrEventsService;

	protected boolean contentChanged;

	public JcrRefreshPolicy(JcrEventsService jcrEventsService, String cacheEntryKey, int time) {
		super(time);
		this.cacheEntryKey = cacheEntryKey;
		if (StringUtils.isBlank(cacheEntryKey)) {
			throw new IllegalArgumentException("Cache entry key can not be blank");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean needsRefresh(CacheEntry entry) {
		return contentChanged || super.needsRefresh(entry);
	}

	@Override
	public boolean contentChanged(String path) {
		if (!contentChanged) {
			for (Pattern pattern : invalidatePaths) {
				if (pattern.matcher(path).matches()) {
					contentChanged = true;
					break;
				}
			}
		}
		return contentChanged;
	}

	@Override
	protected void finalize() throws Throwable {
		jcrEventsService.removeEventListener(this);
		super.finalize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cacheEntryAdded(CacheEntryEvent cacheentryevent) {
		if (cacheEntryKey.equals(cacheentryevent.getKey())) {
			jcrEventsService.removeEventListener(this);
			cacheentryevent.getMap().removeCacheEventListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cacheEntryFlushed(CacheEntryEvent cacheentryevent) {
		if (cacheEntryKey.equals(cacheentryevent.getKey())) {
			jcrEventsService.removeEventListener(this);
			cacheentryevent.getMap().removeCacheEventListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cacheEntryRemoved(CacheEntryEvent cacheentryevent) {
		if (cacheEntryKey.equals(cacheentryevent.getKey())) {
			jcrEventsService.removeEventListener(this);
			cacheentryevent.getMap().removeCacheEventListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cacheEntryUpdated(CacheEntryEvent cacheentryevent) {
		if (cacheEntryKey.equals(cacheentryevent.getKey())) {
			jcrEventsService.removeEventListener(this);
			cacheentryevent.getMap().removeCacheEventListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cacheFlushed(CachewideEvent cachewideevent) {
		jcrEventsService.removeEventListener(this);
		cachewideevent.getCache().removeCacheEventListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cacheGroupFlushed(CacheGroupEvent cachegroupevent) {
		// TODO
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cachePatternFlushed(CachePatternEvent cachepatternevent) {
		if (cacheEntryKey.matches(cachepatternevent.getPattern())) {
			jcrEventsService.removeEventListener(this);
			cachepatternevent.getMap().removeCacheEventListener(this);
		}
	}
}
