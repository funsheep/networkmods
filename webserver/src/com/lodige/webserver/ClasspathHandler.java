/**
 * webserver Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.webserver;

import github.javaappplatform.commons.io.InPipeOut;
import github.javaappplatform.commons.log.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.AsyncContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.WriterOutputStream;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.URIUtil;

/**
 * @author renken
 *
 */
public class ClasspathHandler extends HandlerWrapper implements Handler
{
	
	private static final ClassLoader CLASSLOADER = ClasspathHandler.class.getClassLoader();
	private static final Logger LOGGER = Logger.getLogger();


    private String _cacheControl;
    private MimeTypes _mimeTypes = new MimeTypes();

    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        if (baseRequest.isHandled())
            return;

        boolean skipContentBody = false;

        if(!HttpMethod.GET.is(request.getMethod()))
        {
            if(!HttpMethod.HEAD.is(request.getMethod()))
            {
                //try another handler
                super.handle(target, baseRequest, request, response);
                return;
            }
            skipContentBody = true;
        }

        
        final String resource = getResource(request);
        final URL url = CLASSLOADER.getResource(resource);
        
        // If resource is not found or is directory
        if (url == null || request.getPathInfo().endsWith(URIUtil.SLASH))
        {
            //no resource - try other handlers
            super.handle(target, baseRequest, request, response);
            return;
        }

        // We are going to serve something
        baseRequest.setHandled(true);

        // set the headers
        String mime=this._mimeTypes.getMimeByExtension(resource.toString());
        if (mime==null)
            mime=this._mimeTypes.getMimeByExtension(request.getPathInfo());
        doResponseHeaders(response,resource,mime);
        
        if(skipContentBody)
            return;
        
        // Send the content
        OutputStream out =null;
        try {out = response.getOutputStream();}
        catch(IllegalStateException e) {out = new WriterOutputStream(response.getWriter());}

        // Has the output been wrapped
        if (!(out instanceof HttpOutput))
			try
			{
				InPipeOut.pipe(url.openStream(), out, true);
			}
			catch (InterruptedException e)
			{
				throw new IOException("Contentstream interrupted.", e);
			}
		else
        {
            if (request.isAsyncSupported())
            {
                final AsyncContext async = request.startAsync();
                Callback callback = new Callback()
                {
                    @Override
                    public void succeeded()
                    {
                        async.complete();
                    }

                    @Override
                    public void failed(Throwable x)
                    {
                        LOGGER.warn(x.toString());
                        LOGGER.debug("AsyncContext failed", x);
                        async.complete();
                    }   
                };

                //XXX Here we simply use memory mapped files! This can be a problem for large files!
                ((HttpOutput)out).sendContent(url.openStream(), callback);
            }
            else
            {
                //XXX Here we simply use memory mapped files! This can be a problem for large files!
            	((HttpOutput)out).sendContent(url.openStream());
            }
        }
    }

    protected String getResource(HttpServletRequest request)
    {
        String servletPath;
        String pathInfo;
        boolean included = request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null;
        if (included)
        {
            servletPath = (String)request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
            pathInfo = (String)request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);

            if (servletPath == null && pathInfo == null)
            {
                servletPath = request.getServletPath();
                pathInfo = request.getPathInfo();
            }
        }
        else
        {
            servletPath = request.getServletPath();
            pathInfo = request.getPathInfo();
        }

        String pathInContext=URIUtil.addPaths(servletPath,pathInfo);
        return pathInContext;
    }

    /** Set the response headers.
     * This method is called to set the response headers such as content type and content length.
     * May be extended to add additional headers.
     * @param response
     * @param resource
     * @param mimeType
     */
    protected void doResponseHeaders(HttpServletResponse response, String resource, String mimeType)
    {
        if (mimeType!=null)
            response.setContentType(mimeType);

        if (response instanceof Response)
        {
            HttpFields fields = ((Response)response).getHttpFields();

            if (this._cacheControl!=null)
                fields.put(HttpHeader.CACHE_CONTROL,this._cacheControl);
        }
        else
        {
            if (this._cacheControl!=null)
                response.setHeader(HttpHeader.CACHE_CONTROL.asString(),this._cacheControl);
        }
    }

}
