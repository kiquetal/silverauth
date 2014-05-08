/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.security.oauth2.config.annotation.web.configurers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.ApprovalStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpointHandlerMapping;
import org.springframework.security.oauth2.provider.implicit.ImplicitGrantService;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.implicit.InMemoryImplicitGrantService;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestValidator;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.silverauth.provider.password.ResourceOwnerPasswordTokenGranter;

/**
 * 
 * @author Rob Winch
 * @author Dave Syer
 * @since 2.0
 */
public final class AuthorizationServerEndpointsConfigurer {

	private AuthorizationServerTokenServices tokenServices;

	private ConsumerTokenServices consumerTokenServices;

	private AuthorizationCodeServices authorizationCodeServices;

	private ResourceServerTokenServices resourceTokenServices;

	private ImplicitGrantService implicitGrantService = new InMemoryImplicitGrantService();

	private TokenStore tokenStore;

	private TokenEnhancer tokenEnhancer;

	private AccessTokenConverter accessTokenConverter;

	private ApprovalStore approvalStore;

	private TokenGranter tokenGranter;

	private OAuth2RequestFactory requestFactory;

	private OAuth2RequestValidator requestValidator;

	private UserApprovalHandler userApprovalHandler;

	@SuppressWarnings("unused")
	private AuthenticationManager authenticationManager;

	private ClientDetailsService clientDetailsService;

	private Map<String, String> patternMap = new HashMap<String, String>();

	private FrameworkEndpointHandlerMapping frameworkEndpointHandlerMapping;

	private boolean approvalStoreDisabled;
	
	/** Raoni Modificação **/
	private DataSource dataSource;
	
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	public BCryptPasswordEncoder getbCryptPasswordEncoder() {
		return bCryptPasswordEncoder;
	}

	public void setbCryptPasswordEncoder(BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public AuthorizationServerTokenServices getTokenServices() {
		return tokenServices;
	}

	public TokenStore getTokenStore() {
		return tokenStore();
	}

	public TokenEnhancer getTokenEnhancer() {
		return tokenEnhancer;
	}

	public AccessTokenConverter getAccessTokenConverter() {
		return accessTokenConverter;
	}

	public ApprovalStore getApprovalStore() {
		return approvalStore;
	}

	public ClientDetailsService getClientDetailsService() {
		return clientDetailsService;
	}

	public OAuth2RequestFactory getOAuth2RequestFactory() {
		return requestFactory();
	}

	public OAuth2RequestValidator getOAuth2RequestValidator() {
		return requestValidator();
	}

	public UserApprovalHandler getUserApprovalHandler() {
		return userApprovalHandler();
	}

	public AuthorizationServerEndpointsConfigurer tokenStore(TokenStore tokenStore) {
		this.tokenStore = tokenStore;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer tokenEnhancer(TokenEnhancer tokenEnhancer) {
		this.tokenEnhancer = tokenEnhancer;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer accessTokenConverter(AccessTokenConverter accessTokenConverter) {
		this.accessTokenConverter = accessTokenConverter;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer tokenServices(AuthorizationServerTokenServices tokenServices) {
		this.tokenServices = tokenServices;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer userApprovalHandler(UserApprovalHandler approvalHandler) {
		this.userApprovalHandler = approvalHandler;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer approvalStore(ApprovalStore approvalStore) {
		if (approvalStoreDisabled) {
			throw new IllegalStateException("ApprovalStore was disabled");
		}
		this.approvalStore = approvalStore;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer approvalStoreDisabled() {
		this.approvalStoreDisabled = true;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer pathMapping(String defaultPath, String customPath) {
		this.patternMap.put(defaultPath, customPath);
		return this;
	}

	public AuthorizationServerEndpointsConfigurer authenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer tokenGranter(TokenGranter tokenGranter) {
		this.tokenGranter = tokenGranter;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer clientDetailsService(ClientDetailsService clientDetailsService) {
		this.clientDetailsService = clientDetailsService;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer requestFactory(OAuth2RequestFactory requestFactory) {
		this.requestFactory = requestFactory;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer requestValidator(OAuth2RequestValidator requestValidator) {
		this.requestValidator = requestValidator;
		return this;
	}

	public AuthorizationServerEndpointsConfigurer authorizationCodeServices(
			AuthorizationCodeServices authorizationCodeServices) {
		this.authorizationCodeServices = authorizationCodeServices;
		return this;
	}

	public ConsumerTokenServices getConsumerTokenServices() {
		return consumerTokenServices();
	}

	public ResourceServerTokenServices getResourceServerTokenServices() {
		return resourceTokenServices();
	}

	public ImplicitGrantService getImplicitGrantService() {
		return implicitGrantService;
	}

	public AuthorizationCodeServices getAuthorizationCodeServices() {
		return authorizationCodeServices();
	}

	public OAuth2RequestValidator getRequestValidator() {
		return requestValidator();
	}

	public TokenGranter getTokenGranter() {
		return tokenGranter();
	}

	public FrameworkEndpointHandlerMapping getFrameworkEndpointHandlerMapping() {
		return frameworkEndpointHandlerMapping();
	}

	private ResourceServerTokenServices resourceTokenServices() {
		if (resourceTokenServices == null) {
			if (tokenServices instanceof ResourceServerTokenServices) {
				return (ResourceServerTokenServices) tokenServices;
			}
			resourceTokenServices = createTokenServices();
		}
		return resourceTokenServices;
	}

	private ConsumerTokenServices consumerTokenServices() {
		if (consumerTokenServices == null) {
			if (tokenServices instanceof ConsumerTokenServices) {
				return (ConsumerTokenServices) tokenServices;
			}
			consumerTokenServices = createTokenServices();
		}
		return consumerTokenServices;
	}

	private AuthorizationServerTokenServices tokenServices() {
		if (tokenServices != null) {
			return tokenServices;
		}
		this.tokenServices = createTokenServices();
		return tokenServices;
	}

	private DefaultTokenServices createTokenServices() {
		DefaultTokenServices tokenServices = new DefaultTokenServices();
		tokenServices.setTokenStore(tokenStore());
		tokenServices.setSupportRefreshToken(true);
		tokenServices.setClientDetailsService(clientDetailsService());
		tokenServices.setTokenEnhancer(tokenEnchancer());
		return tokenServices;
	}

	private TokenEnhancer tokenEnchancer() {
		if (this.tokenEnhancer == null && accessTokenConverter() instanceof JwtAccessTokenConverter) {
			tokenEnhancer = (TokenEnhancer) accessTokenConverter;
		}
		return this.tokenEnhancer;
	}

	private AccessTokenConverter accessTokenConverter() {
		if (this.accessTokenConverter == null) {
			accessTokenConverter = new DefaultAccessTokenConverter();
		}
		return this.accessTokenConverter;
	}

	private TokenStore tokenStore() {
		if (tokenStore == null) {
			if (accessTokenConverter() instanceof JwtAccessTokenConverter) {
				this.tokenStore = new JwtTokenStore((JwtAccessTokenConverter) accessTokenConverter());
			}
			else {
				this.tokenStore = new InMemoryTokenStore();
			}
		}
		return this.tokenStore;
	}

	private ApprovalStore approvalStore() {
		if (approvalStore == null && tokenStore() != null && !isApprovalStoreDisabled()) {
			TokenApprovalStore tokenApprovalStore = new TokenApprovalStore();
			tokenApprovalStore.setTokenStore(tokenStore());
			this.approvalStore = tokenApprovalStore;
		}
		return this.approvalStore;
	}

	private boolean isApprovalStoreDisabled() {
		return approvalStoreDisabled || (tokenStore() instanceof JwtTokenStore);
	}

	private ClientDetailsService clientDetailsService() {
		if (clientDetailsService == null) {
			this.clientDetailsService = new InMemoryClientDetailsService();
		}
		return this.clientDetailsService;
	}

	private UserApprovalHandler userApprovalHandler() {
		if (userApprovalHandler == null) {
			if (approvalStore() != null) {
				ApprovalStoreUserApprovalHandler handler = new ApprovalStoreUserApprovalHandler();
				handler.setApprovalStore(approvalStore());
				handler.setRequestFactory(requestFactory());
				handler.setClientDetailsService(clientDetailsService);
				this.userApprovalHandler = handler;
			}
			else if (tokenStore() != null) {
				TokenStoreUserApprovalHandler userApprovalHandler = new TokenStoreUserApprovalHandler();
				userApprovalHandler.setTokenStore(tokenStore());
				userApprovalHandler.setClientDetailsService(clientDetailsService());
				userApprovalHandler.setRequestFactory(requestFactory());
				this.userApprovalHandler = userApprovalHandler;
			}
			else {
				throw new IllegalStateException("Either a TokenStore or an ApprovalStore must be provided");
			}
		}
		return this.userApprovalHandler;
	}

	private AuthorizationCodeServices authorizationCodeServices() {
		if (authorizationCodeServices == null) {
			authorizationCodeServices = new InMemoryAuthorizationCodeServices();
		}
		return authorizationCodeServices;
	}

	private OAuth2RequestFactory requestFactory() {
		if (requestFactory != null) {
			return requestFactory;
		}
		requestFactory = new DefaultOAuth2RequestFactory(clientDetailsService());
		return requestFactory;
	}

	private OAuth2RequestValidator requestValidator() {
		if (requestValidator != null) {
			return requestValidator;
		}
		requestValidator = new DefaultOAuth2RequestValidator();
		return requestValidator;
	}

	private TokenGranter tokenGranter() {
		if (tokenGranter == null) {
			ClientDetailsService clientDetails = clientDetailsService();
			AuthorizationServerTokenServices tokenServices = tokenServices();
			AuthorizationCodeServices authorizationCodeServices = authorizationCodeServices();
			OAuth2RequestFactory requestFactory = requestFactory();

			List<TokenGranter> tokenGranters = new ArrayList<TokenGranter>();
			tokenGranters.add(new AuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices,
					clientDetails, requestFactory));
			tokenGranters.add(new RefreshTokenGranter(tokenServices, clientDetails, requestFactory));
			ImplicitTokenGranter implicit = new ImplicitTokenGranter(tokenServices, clientDetails, requestFactory);
			implicit.setImplicitGrantService(implicitGrantService);
			tokenGranters.add(implicit);
			tokenGranters.add(new ClientCredentialsTokenGranter(tokenServices, clientDetails, requestFactory));
			
			/**  Modificação Raoni **/
			tokenGranters.add(new ResourceOwnerPasswordTokenGranter(bCryptPasswordEncoder, dataSource, tokenServices,
					clientDetails, requestFactory));
			
			tokenGranter = new CompositeTokenGranter(tokenGranters);
		}
		return tokenGranter;
	}

	private FrameworkEndpointHandlerMapping frameworkEndpointHandlerMapping() {
		if (frameworkEndpointHandlerMapping == null) {
			frameworkEndpointHandlerMapping = new FrameworkEndpointHandlerMapping();
			frameworkEndpointHandlerMapping.setMappings(patternMap);
		}
		return frameworkEndpointHandlerMapping;
	}
}
