package com.cognifide.cq.cache.tag;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;

import com.cognifide.cq.cache.filter.ComponentCacheFilter;
import com.cognifide.cq.cache.model.CacheKeyGenerator;
import com.cognifide.cq.cache.model.CacheKeyGeneratorImpl;
import com.cognifide.cq.cache.model.PathAliasStore;
import com.cognifide.cq.cache.refresh.jcr.JcrEventsService;
import com.cognifide.cq.cache.refresh.jcr.TagJcrRefreshPolicy;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.web.ServletCacheAdministrator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @author Bartosz Rudnicki
 */
public class CacheTag extends BodyTagSupport {

	private static final long serialVersionUID = 4931603091638059329L;

	private transient final CacheKeyGenerator keyGenerator = new CacheKeyGeneratorImpl();

	private String invalidationPaths;

	private int time = Integer.MIN_VALUE;

	private String key;

	private String currentKey;

	private boolean invalidationSelf;

	private int cacheLevel;

	private JcrEventsService jcrEventsService;

	private PathAliasStore pathAliasStores;

	public CacheTag() {
		reset();
		resolveServiceDependencies();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int doAfterBody() throws JspException {
		if (isEnabled()) {
			ServletCacheAdministrator admin = ServletCacheAdministrator.getInstance(pageContext
					.getServletContext());
			Cache cache = admin.getAppScopeCache(pageContext.getServletContext());

			String content = bodyContent.getString();
			try {
				bodyContent.writeOut(bodyContent.getEnclosingWriter());
			} catch (IOException ioException) {
				throw new JspException(ioException);
			}

			if (time == Integer.MIN_VALUE) {
				time = getDefaultDuration();
			}

			TagJcrRefreshPolicy policy
					= new TagJcrRefreshPolicy(jcrEventsService, pathAliasStores, getKey(), time, getSelfPagePath(),
							invalidationPaths);
			cache.putInCache(getKey(), content, policy);
			jcrEventsService.addEventListener(policy);
			cache.addCacheEventListener(policy);
		}

		return 0;
	}

	@Override
	public int doEndTag() throws JspException {
		reset();
		return EVAL_PAGE;
	}

	private String getSelfPagePath() {
		if (invalidationSelf && (pageContext.getRequest() instanceof SlingHttpServletRequest)) {
			String path = ((SlingHttpServletRequest) pageContext.getRequest()).getResource().getPath();
			if (path.indexOf("/jcr:content") > 0) {
				path = path.substring(0, path.indexOf("/jcr:content"));
			}
			return path;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int doStartTag() throws JspException {
		if (isEnabled()) {
			ServletCacheAdministrator admin = ServletCacheAdministrator.getInstance(pageContext
					.getServletContext());
			Cache cache = admin.getAppScopeCache(pageContext.getServletContext());
			try {
				String content = (String) cache.getFromCache(getKey());
				try {
					pageContext.getOut().write(content);
				} catch (IOException ioException) {
					throw new JspException(ioException);
				}
				return SKIP_BODY;
			} catch (NeedsRefreshException needsRefreshException) {
				return EVAL_BODY_BUFFERED;
			}
		} else {
			return EVAL_BODY_INCLUDE;
		}
	}

	public void setInvalidationPaths(String invalidationPaths) {
		this.invalidationPaths = invalidationPaths;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setKey(String key) {
		if (StringUtils.isBlank(key)) {
			throw new IllegalArgumentException("Key can not be blank");
		}
		this.key = key;
	}

	public void setInvalidationSelf(boolean invalidationSelf) {
		this.invalidationSelf = invalidationSelf;
	}

	public void setCacheLevel(int cacheLevel) {
		this.cacheLevel = cacheLevel;
	}

	protected String getKey() {
		if (StringUtils.isBlank(currentKey)) {
			if (StringUtils.isBlank(key)) {
				throw new IllegalStateException("Key can not be blank");
			}

			if (pageContext.getRequest() instanceof HttpServletRequest) {
				HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
				String pagePath = request.getRequestURI();
				String selectorString = "";
				int fisrtDot = pagePath.indexOf('.');
				int lastDot = pagePath.lastIndexOf('.');
				if (fisrtDot > 0) {
					if (lastDot > fisrtDot) {
						selectorString = pagePath.substring(fisrtDot + 1, lastDot);
					}
					pagePath = pagePath.substring(0, fisrtDot);
				}
				currentKey = keyGenerator.generateKey(cacheLevel, key, pagePath, selectorString);
			} else {
				throw new IllegalStateException(
						"Request is expected to be a SlingHttpServletRequest or a HttpServletRequest");
			}
		}
		return currentKey;
	}

	protected boolean isEnabled() {
		if (pageContext.getServletContext().getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED) == null) {
			return false;
		} else {
			return (Boolean) pageContext.getServletContext().getAttribute(
					ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED);
		}
	}

	protected int getDefaultDuration() {
		if (pageContext.getServletContext().getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION) == null) {
			return -1;
		} else {
			return (Integer) pageContext.getServletContext().getAttribute(
					ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION);
		}
	}

	private void reset() {
		key = null;
		currentKey = null;
		invalidationPaths = null;
		time = Integer.MIN_VALUE;
		invalidationSelf = true;
		cacheLevel = -1;
	}

	private void resolveServiceDependencies() {
		this.jcrEventsService = getService(JcrEventsService.class);
		this.pathAliasStores = getService(PathAliasStore.class);
	}

	@SuppressWarnings("unchecked")
	private <T> T getService(Class<T> serviceClass) {
		T result = null;
		BundleContext bundleContext = FrameworkUtil.getBundle(serviceClass).getBundleContext();
		if (null != bundleContext) {
			ServiceReference serviceReference = bundleContext.getServiceReference(serviceClass.getName());
			result = (T) bundleContext.getService(serviceReference);
		}
		return result;
	}
}
