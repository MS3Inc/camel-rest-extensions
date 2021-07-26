package com.ms3_inc.tavros.extensions.rest;

/*-
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;;
import org.springframework.util.MimeType;
import org.springframework.util.ObjectUtils;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * A set of MediaType utilities.
 *
 * <p>
 * This file is a derived work of org.springframework.util.MimeType
 * Spring Framework v5.3.0-M1. Modifications made to the original work include:
 * <li>Utility `includes` and `isCompatibleWith` methods that account for parameters</li>
 * </p>
 *
 * @author Arjen Poutsma (2002-2020)
 * @author Juergen Hoeller (2002-2020)
 * @author Rossen Stoyanchev (2002-2020)
 * @author Sebastien Deleuze (2002-2020)
 * @author Kazuki Shimizu (2002-2020)
 * @author Sam Brannen (2002-2020)
 * @author Jose Montoya
 * @see MimeType
 * @see <a href=https://github.com/spring-projects/spring-framework/pull/1920/>
 * @since 0.1.7
 */
public class MediaTypeUtils {
    private static final String PARAM_CHARSET = "charset";
    protected static final String WILDCARD_TYPE = "*";

    /**
     * Indicate whether this MIME Type includes the given MIME Type.
     * <p>For instance, {@code text/*} includes {@code text/plain} and {@code text/html},
     * and {@code application/*+xml} includes {@code application/soap+xml}, etc.
     * This method is <b>not</b> symmetric.
     * @param other the reference MIME Type with which to compare
     * @return {@code true} if this MIME Type includes the given MIME Type;
     * {@code false} otherwise
     */
    public static boolean includes(@NonNull MimeType one, @Nullable MimeType other) {
        if (other == null) {
            return false;
        }
        if (one.isWildcardType()) {
            // */* includes anything
            return true;
        }
        else if (one.getType().equals(other.getType())) {
            if (one.getSubtype().equals(other.getSubtype())) {
                return parametersInclude(one, other);
            }
            if (one.isWildcardSubtype()) {
                // Wildcard with suffix, e.g. application/*+xml
                int thisPlusIdx = one.getSubtype().lastIndexOf('+');
                if (thisPlusIdx == -1) {
                    return true;
                }
                else {
                    // application/*+xml includes application/soap+xml
                    int otherPlusIdx = other.getSubtype().lastIndexOf('+');
                    if (otherPlusIdx != -1) {
                        String thisSubtypeNoSuffix = one.getSubtype().substring(0, thisPlusIdx);
                        String thisSubtypeSuffix = one.getSubtype().substring(thisPlusIdx + 1);
                        String otherSubtypeSuffix = other.getSubtype().substring(otherPlusIdx + 1);
                        if (thisSubtypeSuffix.equals(otherSubtypeSuffix) && WILDCARD_TYPE.equals(thisSubtypeNoSuffix)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determine if the parameters in this {@code MimeType} include those
     * of the supplied {@code MimeType}, performing case-insensitive comparisons
     * for {@link Charset}s.
     * <p>Parameters are not included when this contains more parameters than
     * the supplied, when this contains a parameter that the supplied does not,
     * or when they both contain the same parameter with different values.</p>
     * @since 5.10.0
     */
    private static boolean parametersInclude(MimeType one, MimeType other) {
        if (one.getParameters().size() > other.getParameters().size()) {
            return false;
        }

        for (Map.Entry<String, String> entry : one.getParameters().entrySet()) {
            String key = entry.getKey();
            if (!other.getParameters().containsKey(key)) {
                return false;
            }
            if (PARAM_CHARSET.equals(key)) {
                if (one.getCharset() != null && !one.getCharset().equals(other.getCharset()))
                    return false;
            }
            else if (!ObjectUtils.nullSafeEquals(entry.getValue(), other.getParameters().get(key))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indicate whether this MIME Type is compatible with the given MIME Type.
     * <p>For instance, {@code text/*} is compatible with {@code text/plain},
     * {@code text/html}, and vice versa. In effect, this method is similar to
     * {@link #includes}, except that it <b>is</b> symmetric.
     * @param other the reference MIME Type with which to compare
     * @return {@code true} if this MIME Type is compatible with the given MIME Type;
     * {@code false} otherwise
     */
    public boolean isCompatibleWith(MimeType one, @Nullable MimeType other) {
        if (other == null) {
            return false;
        }
        if (one.isWildcardType() || other.isWildcardType()) {
            return true;
        }
        else if (one.getType().equals(other.getType())) {
            if (one.getSubtype().equals(other.getSubtype())) {
                return parametersAreCompatibleWith(one, other);
            }
            // Wildcard with suffix? e.g. application/*+xml
            if (one.isWildcardSubtype() || other.isWildcardSubtype()) {
                int thisPlusIdx = one.getSubtype().lastIndexOf('+');
                int otherPlusIdx = other.getSubtype().lastIndexOf('+');
                if (thisPlusIdx == -1 && otherPlusIdx == -1) {
                    return true;
                }
                else if (thisPlusIdx != -1 && otherPlusIdx != -1) {
                    String thisSubtypeNoSuffix = one.getSubtype().substring(0, thisPlusIdx);
                    String otherSubtypeNoSuffix = other.getSubtype().substring(0, otherPlusIdx);
                    String thisSubtypeSuffix = one.getSubtype().substring(thisPlusIdx + 1);
                    String otherSubtypeSuffix = other.getSubtype().substring(otherPlusIdx + 1);
                    if (thisSubtypeSuffix.equals(otherSubtypeSuffix) &&
                            (WILDCARD_TYPE.equals(thisSubtypeNoSuffix) || WILDCARD_TYPE.equals(otherSubtypeNoSuffix))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determine if the parameters in this {@code MimeType} and the supplied
     * {@code MimeType} are compatible, performing case-insensitive comparisons
     * for {@link Charset}s.
     * <p>Parameters are incompatible when they contain the same parameter
     * with different values.</p>
     * @since 5.10.0
     */
    private static boolean parametersAreCompatibleWith(MimeType one, MimeType other) {
        for (Map.Entry<String, String> entry : one.getParameters().entrySet()) {
            String key = entry.getKey();
            if (PARAM_CHARSET.equals(key)) {
                if (other.getCharset() != null && !other.getCharset().equals(one.getCharset()))
                    return false;
            }
            else if (other.getParameters().containsKey(key) && !entry.getValue().equals(other.getParameters().get(key)))
                return false;
        }

        return true;
    }
}
