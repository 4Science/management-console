/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mainwebapp.home;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.duracloud.mainwebapp.control.SubDirController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import junit.framework.TestCase;

public class SubDirControllerTest
        extends TestCase {

    private MockHttpServletRequest request;

    private HttpServletResponse response;

    @Override
    @Before
    protected void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/home.htm");

        response = new MockHttpServletResponse();
    }

    @Override
    @After
    protected void tearDown() {
        request = null;
        response = null;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleRequestView() throws Exception {

        SubDirController controller = new SubDirController();
        ModelAndView modelAndView = controller.handleRequest(request, response);
        assertNotNull(modelAndView);

        String viewName = modelAndView.getViewName();
        assertNotNull(viewName);
        assertEquals("home", viewName);

        Map model = modelAndView.getModel();
        assertNotNull(model);

        String nowValue = (String) model.get("now");
        assertNotNull(nowValue);
    }
}