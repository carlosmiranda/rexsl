/**
 * Copyright (c) 2011-2012, ReXSL.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the ReXSL.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rexsl.core;

import com.ymock.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import javax.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;

/**
 * Wrapper around {@code MANIFEST.MF} files.
 *
 * The class will read all {@code MANIFEST.MF} files available in classpath
 * and all attributes from them. This mechanism is very useful for sending
 * information from continuous integration environment to the production
 * environment. For example, you want your site to show project version and
 * the date of WAR file packaging. First, you configure
 * {@code maven-war-plugin} to add this information to {@code MANIFEST.MF}:
 *
 * <pre>
 * &lt;plugin>
 *  &lt;artifactId>maven-war-plugin&lt;/artifactId>
 *  &lt;configuration>
 *   &lt;archive>
 *    &lt;manifestEntries>
 *     &lt;Foo-Version>${project.version}&lt;/Foo-Version>
 *     &lt;Foo-Date>${maven.build.timestamp}&lt;/Foo-Date>
 *    &lt;/manifestEntries>
 *   &lt;/archive>
 *  &lt;/configuration>
 * &lt;/plugin>
 * </pre>
 *
 * <p>{@code maven-war-plugin} will add these attributes to your
 * {@code MANIFEST.MF} file and the
 * project will be deployed to the production environment. Then, you can read
 * these attributes where it's necessary (in one of your JAXB annotated objects,
 * for example) and show to users:
 *
 * <pre>
 * import com.rexsl.core.Manifest;
 * import java.text.SimpleDateFormat;
 * import java.util.Date;
 * import java.util.Locale;
 * import javax.xml.bind.annotation.XmlElement;
 * import javax.xml.bind.annotation.XmlRootElement;
 * &#64;XmlRootElement
 * public final class Page {
 *   &#64;XmlElement
 *   public String version() {
 *    return Manifests.read("Foo-Version");
 *   }
 *   &#64;XmlElement
 *   public Date date() {
 *    return new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH).parse(
 *     Manifests.read("Foo-Date");
 *    );
 *   }
 * }
 * </pre>
 *
 * <p>In unit and integration tests you may need to inject some values
 * to {@code MANIFEST.MF} in runtime (for example, in your bootstrap Groovy
 * scripts):
 *
 * <pre>
 * import com.rexsl.core.Manifests
 * Manifests.inject("Foo-URL", "http://localhost/abc");
 * </pre>
 *
 * <p>When it is necessary to isolate such injections between different unit
 * tests "snapshots" may help, for example (it's a method in a unit test):
 *
 * <pre>
 * &#64;Test
 * public void testSomeCode() {
 *   // save current state of all MANIFEST.MF attributes
 *   final byte[] snapshot = Manifests.snapshot();
 *   // inject new attribute required for this specific test
 *   Manifests.inject("Foo-URL", "http://localhost/abc");
 *   // restore back all attributes, as they were before the injection
 *   Manifests.revert(snapshot);
 * }
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @author Prasath Premkumar (popprem@gmail.com)
 * @version $Id$
 * @see <a href="http://download.oracle.com/javase/1,5.0/docs/guide/jar/jar.html#JAR%20Manifest">JAR Manifest</a>
 * @see <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven Archiver</a>
 * @see <a href="http://trac.fazend.com/rexsl/ticket/55">Class was introduced in ticket #55</a>
 * @since 0.3
 */
@SuppressWarnings("PMD.UseConcurrentHashMap")
public final class Manifests {

    /**
     * Injected attributes.
     * @see #inject(String,String)
     */
    private static final Map<String, String> INJECTED =
        new ConcurrentHashMap<String, String>();

    /**
     * Attributes retrieved from all existing {@code MANIFEST.MF} files.
     * @see #load()
     */
    private static Map<String, String> attributes = Manifests.load();

    /**
     * Failures registered during loading.
     * @see #load()
     */
    private static Map<URI, String> failures;

    /**
     * It's a utility class, can't be instantiated.
     */
    private Manifests() {
        // intentionally empty
    }

    /**
     * Read one attribute available in one of {@code MANIFEST.MF} files.
     *
     * <p>If such a attribute doesn't exist {@link IllegalArgumentException}
     * will be thrown. If you're not sure whether the attribute is present or
     * not use {@link #exists(String)} beforehand.
     *
     * <p>During testing you can inject attributes into this class by means
     * of {@link #inject(String,String)}.
     *
     * @param name Name of the attribute
     * @return The value of the attribute retrieved
     */
    public static String read(final String name) {
        if (Manifests.attributes == null) {
            throw new IllegalArgumentException(
                "Manifests haven't been loaded yet by request from XsltFilter"
            );
        }
        if (!Manifests.exists(name)) {
            final StringBuilder bldr = new StringBuilder(
                Logger.format(
                    // @checkstyle LineLength (1 line)
                    "Atribute '%s' not found in MANIFEST.MF file(s) among %d other attribute(s) %[list]s and %d injection(s)",
                    name,
                    Manifests.attributes.size(),
                    new TreeSet<String>(Manifests.attributes.keySet()),
                    Manifests.INJECTED.size()
                )
            );
            if (!Manifests.failures.isEmpty()) {
                bldr.append("; failures: ").append(
                    Logger.format("%[list]s", Manifests.failures.keySet())
                );
            }
            throw new IllegalArgumentException(bldr.toString());
        }
        String result;
        if (Manifests.INJECTED.containsKey(name)) {
            result = Manifests.INJECTED.get(name);
        } else {
            result = Manifests.attributes.get(name);
        }
        return result;
    }

    /**
     * Inject new attribute.
     *
     * <p>An attribute can be injected in runtime, mostly for the sake of
     * unit and integration testing. Once injected an attribute becomes
     * available with {@link #read(String)}.
     *
     * @param name Name of the attribute
     * @param value The value of the attribute being injected
     */
    public static void inject(final String name, final String value) {
        if (Manifests.INJECTED.containsKey(name)) {
            Logger.info(
                Manifests.class,
                "#inject(%s, '%s'): replaced previous injection '%s'",
                name,
                value,
                Manifests.INJECTED.get(name)
            );
        } else {
            Logger.info(
                Manifests.class,
                "#inject(%s, '%s'): injected",
                name,
                value
            );
        }
        Manifests.INJECTED.put(name, value);
    }

    /**
     * Check whether attribute exists in any of {@code MANIFEST.MF} files.
     *
     * <p>Use this method before {@link #read(String)} to check whether an
     * attribute exists, in order to avoid a runtime exception.
     *
     * @param name Name of the attribute to check
     * @return Returns {@code TRUE} if it exists, {@code FALSE} otherwise
     */
    public static boolean exists(final String name) {
        return Manifests.attributes.containsKey(name)
            || Manifests.INJECTED.containsKey(name);
    }

    /**
     * Make a snapshot of current attributes and their values.
     * @return The snapshot, to be used later with {@link #revert(byte[])}
     * @see <a href="http://trac.fazend.com/rexsl/ticket/107">Introduced in ticket #107</a>
     */
    public static byte[] snapshot() {
        byte[] snapshot;
        synchronized (Manifests.INJECTED) {
            snapshot = SerializationUtils.serialize(
                (Serializable) Manifests.INJECTED
            );
        }
        Logger.debug(
            Manifests.class,
            "#snapshot(): created (%d bytes)",
            snapshot.length
        );
        return snapshot;
    }

    /**
     * Revert to the state that was recorded by {@link #snapshot()}.
     * @param snapshot The snapshot taken by {@link #snapshot()}
     * @see <a href="http://trac.fazend.com/rexsl/ticket/107">Introduced in ticket #107</a>
     */
    public static void revert(final byte[] snapshot) {
        synchronized (Manifests.INJECTED) {
            Manifests.INJECTED.clear();
            Manifests.INJECTED.putAll(
                (Map<String, String>) SerializationUtils.deserialize(snapshot)
            );
        }
        Logger.debug(
            Manifests.class,
            "#revert(%d bytes): reverted",
            snapshot.length
        );
    }

    /**
     * Append attributes from the web application {@code MANIFEST.MF}, called
     * from {@link XsltFilter#init(FilterConfig)}.
     *
     * <p>You can call this method in your own
     * {@link javax.servlet.Filter} or
     * {@link javax.servlet.ServletContextListener},
     * in order to inject {@code MANIFEST.MF} attributes to the class.
     *
     * @param ctx Servlet context
     * @see #Manifests()
     */
    public static void append(final ServletContext ctx) {
        final long start = System.nanoTime();
        URL main;
        try {
            main = ctx.getResource("/META-INF/MANIFEST.MF");
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        if (main == null) {
            Logger.warn(
                Manifests.class,
                "#append(%s): MANIFEST.MF not found in WAR package",
                ctx.getClass().getName()
            );
        } else {
            final Map<String, String> attrs;
            try {
                attrs = Manifests.loadOneFile(main);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            Manifests.attributes.putAll(attrs);
            Logger.info(
                Manifests.class,
                "#append(%s): %d attribs loaded from %s in %[nano]s: %[list]s",
                ctx.getClass().getName(),
                attrs.size(),
                main,
                System.nanoTime() - start,
                new TreeSet<String>(attrs.keySet())
            );
        }
    }

    /**
     * Append attributes from the file.
     * @param file The file to load attributes from
     */
    public static void append(final File file) {
        final long start = System.nanoTime();
        Map<String, String> attrs;
        try {
            attrs = Manifests.loadOneFile(file.toURI().toURL());
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        Manifests.attributes.putAll(attrs);
        Logger.info(
            Manifests.class,
            "#append('%s'): %d attributes loaded in %[nano]s: %[list]s",
            file,
            attrs.size(),
            System.nanoTime() - start,
            new TreeSet<String>(attrs.keySet())
        );
    }

    /**
     * Load attributes from classpath.
     * @return All found attributes
     */
    private static Map<String, String> load() {
        final long start = System.nanoTime();
        Manifests.failures = new ConcurrentHashMap<URI, String>();
        final Map<String, String> attrs =
            new ConcurrentHashMap<String, String>();
        Integer count = 0;
        for (URI uri : Manifests.uris()) {
            try {
                attrs.putAll(Manifests.loadOneFile(uri.toURL()));
            } catch (IOException ex) {
                Manifests.failures.put(uri, ex.getMessage());
                Logger.error(
                    Manifests.class,
                    "#load(): '%s' failed %[exception]s",
                    uri,
                    ex
                );
            }
            ++count;
        }
        Logger.info(
            Manifests.class,
            "#load(): %d attribs loaded from %d URL(s) in %[nano]s: %[list]s",
            attrs.size(),
            count,
            System.nanoTime() - start,
            new TreeSet<String>(attrs.keySet())
        );
        return attrs;
    }

    /**
     * Find all URLs.
     * @return The list of URLs
     * @see #load()
     */
    private static Set<URI> uris() {
        Enumeration<URL> resources;
        try {
            resources = Thread.currentThread().getContextClassLoader()
                .getResources("META-INF/MANIFEST.MF");
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        final Set<URI> uris = new HashSet<URI>();
        while (resources.hasMoreElements()) {
            try {
                uris.add(resources.nextElement().toURI());
            } catch (URISyntaxException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return uris;
    }

    /**
     * Load attributes from one file.
     *
     * <p>Inside the method we catch {@code RuntimeException} (which may look
     * suspicious) in order to protect our execution flow from expected (!)
     * exceptions from {@link Manifest#getMainAttributes()}. For some reason,
     * this JDK method doesn't throw checked exceptions if {@code MANIFEST.MF}
     * file format is broken. Instead, it throws a runtime exception (an
     * unchecked one), which we should catch in such an inconvenient way.
     *
     * @param url The URL of it
     * @return The attributes loaded
     * @see #load()
     * @see tickets #193 and #323
     * @throws IOException If some problem happens
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private static Map<String, String> loadOneFile(final URL url)
        throws IOException {
        final Map<String, String> props =
            new ConcurrentHashMap<String, String>();
        final InputStream stream = url.openStream();
        try {
            final Manifest manifest = new Manifest(stream);
            final Attributes attrs = manifest.getMainAttributes();
            for (Object key : attrs.keySet()) {
                final String value = attrs.getValue((Name) key);
                props.put(key.toString(), value);
            }
            Logger.trace(
                Manifests.class,
                "#loadOneFile('%s'): %d attributes loaded (%[list]s)",
                url,
                props.size(),
                new TreeSet<String>(props.keySet())
            );
        // @checkstyle IllegalCatch (1 line)
        } catch (RuntimeException ex) {
            Logger.error(
                Manifests.class,
                "#getMainAttributes(): '%s' failed %[exception]s",
                url,
                ex
            );
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return props;
    }

}
