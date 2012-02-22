/*
 * Copyright (c) 2011 Socialize Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.socialize.auth;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jason Polites
 *
 */
public class DefaultUserProviderCredentialsMap implements UserProviderCredentialsMap {
	
	private Map<AuthProviderType, UserProviderCredentials> map;

	public DefaultUserProviderCredentialsMap() {
		super();
		map = new LinkedHashMap<AuthProviderType, UserProviderCredentials>(5);
	}

	/*
	 * (non-Javadoc)
	 * @see com.socialize.auth.UserProviderCredentialsMap#get(com.socialize.auth.AuthProviderType)
	 */
	@Override
	public UserProviderCredentials get(AuthProviderType type) {
		return map.get(type);
	}

	/*
	 * (non-Javadoc)
	 * @see com.socialize.auth.UserProviderCredentialsMap#put(com.socialize.auth.AuthProviderType, com.socialize.auth.UserProviderCredentials)
	 */
	@Override
	public void put(AuthProviderType type, UserProviderCredentials data) {
		map.put(type, data);
	}

	/* (non-Javadoc)
	 * @see com.socialize.auth.UserProviderCredentialsMap#values()
	 */
	@Override
	public Collection<UserProviderCredentials> values() {
		return map.values();
	}

}
