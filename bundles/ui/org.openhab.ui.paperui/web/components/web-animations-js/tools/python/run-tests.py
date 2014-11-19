#!/usr/bin/python
#
# -*- coding: utf-8 -*-
# vim: set ts=4 sw=4 et sts=4 ai:

import atexit
import base64
import cStringIO as StringIO
import getpass
import httplib
import json as simplejson
import os
import platform
import pprint
import re
import socket
import sys
import time
import urllib2
import zipfile

import argparse
parser = argparse.ArgumentParser()

parser.add_argument(
    "-b", "--browser", type=str, required=True,
    choices=['Firefox', 'Chrome', 'Ie', 'PhantomJS', 'Remote'],
    help="Which WebDriver to use.")

parser.add_argument(
    "-f", "--flag", action='append', default=[],
    help="Command line flags to pass to the browser, "
         "currently only available for Chrome. "
         "Each flag must be a separate --flag invoccation.")

parser.add_argument(
    "-x", "--virtual", action='store_true', default=False,
    help="Use a virtual screen system such as Xvfb, Xephyr or Xvnc.")

parser.add_argument(
    "-d", "--dontexit", action='store_true', default=False,
    help="At end of testing, don't exit.")

parser.add_argument(
    "-v", "--verbose", action='store_true', default=False,
    help="Output more information.")

parser.add_argument(
    "-u", "--upload", action='store_true', default=False,
    help="Upload images to picture sharing site (http://postimage.org/),"
         " only really useful for testbots.")

# Only used by the Remote browser option.
parser.add_argument(
    "--remote-executor", type=str,
    help="Location of the Remote executor.")

parser.add_argument(
    "--remote-caps", action='append',
    help="Location of capabilities to request on Remote executor.",
    default=[])

parser.add_argument(
    "-s", "--sauce", action='store_true', default=False,
    help="Use the SauceLab's Selenium farm rather then locally starting"
         " selenium. SAUCE_USERNAME and SAUCE_ACCESS_KEY must be set in"
         " environment.")

# Subunit / testrepository support
parser.add_argument(
    "--subunit", action='store_true', default=False,
    help="Output raw subunit binary data.")

parser.add_argument(
    "--list", action='store_true', default=False,
    help="List tests which are available.")

parser.add_argument(
    "--load-list", type=argparse.FileType('r'),
    help="List of tests to run.")

args = parser.parse_args()

if args.verbose and args.subunit:
    raise SystemExit("--verbose and --subunit are not compatible.")

# Make sure the repository is setup and the dependencies exist
# -----------------------------------------------------------------------------

import subprocess

caps = {}
if not args.sauce:
    # Get any selenium drivers we might need
    if args.browser == "Chrome":
        # Get ChromeDriver if it's not in the path...
        # https://code.google.com/p/chromedriver/downloads/list
        chromedriver_bin = "chromedriver"
        chromedriver_url_tmpl = "http://chromedriver.storage.googleapis.com/2.6/chromedriver_%s%s.zip"  # noqa

        if platform.system() == "Linux":
            if platform.processor() == "x86_64":
                # 64 bit binary needed
                chromedriver_url = chromedriver_url_tmpl % ("linux", "64")
            else:
                # 32 bit binary needed
                chromedriver_url = chromedriver_url_tmpl % ("linux", "32")

        elif platform.system() == "Darwin":
            chromedriver_url = chromedriver_url_tmpl % ("mac", "32")
        elif platform.system() == "win32":
            chromedriver_url = chromedriver_url_tmpl % ("win", "32")
            chromedriver_url = "chromedriver.exe"

        try:
            if subprocess.call(chromedriver_bin) != 0:
                raise OSError("Return code?")
        except OSError:
            chromedriver_local = os.path.join("tools", chromedriver_bin)

            if not os.path.exists(chromedriver_local):
                datafile = StringIO.StringIO(
                    urllib2.urlopen(chromedriver_url).read())
                contents = zipfile.ZipFile(datafile, 'r')
                contents.extract(chromedriver_bin, "tools")

            chromedriver = os.path.realpath(chromedriver_local)
            os.chmod(chromedriver, 0755)
        else:
            chromedriver = "chromedriver"

    elif args.browser == "Firefox":
        pass

    elif args.browser == "PhantomJS":
        phantomjs_bin = None
        if platform.system() == "Linux":
            phantomjs_bin = "phantomjs"
            if platform.processor() == "x86_64":
                # 64 bit binary needed
                phantomjs_url = "https://phantomjs.googlecode.com/files/phantomjs-1.9.0-linux-x86_64.tar.bz2"  # noqa
            else:
                # 32 bit binary needed
                phantomjs_url = "https://phantomjs.googlecode.com/files/phantomjs-1.9.0-linux-i686.tar.bz2"  # noqa

            phantomjs_local = os.path.join("tools", phantomjs_bin)
            if not os.path.exists(phantomjs_local):
                datafile = StringIO.StringIO(
                    urllib2.urlopen(phantomjs_url).read())
                contents = tarfile.TarFile.open(fileobj=datafile, mode='r:bz2')
                file("tools/"+phantomjs_bin, "w").write(
                    contents.extractfile(
                        "phantomjs-1.9.0-linux-x86_64/bin/"+phantomjs_bin
                    ).read())

            phantomjs = os.path.realpath(phantomjs_local)
            os.chmod(phantomjs, 0755)
        else:
            if platform.system() == "Darwin":
                phantomjs_url = "https://phantomjs.googlecode.com/files/phantomjs-1.9.0-macosx.zip"  # noqa
                phantomjs_bin = "phantomjs"

            elif platform.system() == "win32":
                chromedriver_bin = "https://phantomjs.googlecode.com/files/phantomjs-1.9.0-windows.zip"  # noqa
                phantomjs_url = "phantomjs.exe"

            phantomjs_local = os.path.join("tools", phantomjs_bin)
            if not os.path.exists(phantomjs_local):
                datafile = StringIO.StringIO(
                    urllib2.urlopen(phantomjs_url).read())
                contents = zipfile.ZipFile(datafile, 'r')
                contents.extract(phantomjs_bin, "tools")

            phantomjs = os.path.realpath(phantomjs_local)
            os.chmod(phantomjs, 0755)
else:
    assert os.environ['SAUCE_USERNAME']
    assert os.environ['SAUCE_ACCESS_KEY']
    sauce_username = os.environ['SAUCE_USERNAME']
    sauce_access_key = os.environ['SAUCE_ACCESS_KEY']

    # Download the Sauce Connect script
    sauce_connect_url = "http://saucelabs.com/downloads/Sauce-Connect-latest.zip"  # noqa
    sauce_connect_bin = "Sauce-Connect.jar"
    sauce_connect_local = os.path.join("tools", sauce_connect_bin)
    if not os.path.exists(sauce_connect_local):
        datafile = StringIO.StringIO(urllib2.urlopen(sauce_connect_url).read())
        contents = zipfile.ZipFile(datafile, 'r')
        contents.extract(sauce_connect_bin, "tools")

    if 'TRAVIS_JOB_NUMBER' in os.environ:
        tunnel_id = os.environ['TRAVIS_JOB_NUMBER']
    else:
        tunnel_id = "%s:%s" % (socket.gethostname(), os.getpid())
    args.remote_caps.append('tunnel-identifier=%s' % tunnel_id)

    # Kill the tunnel when we die
    def kill_tunnel(sauce_tunnel):
        if sauce_tunnel.returncode is None:
            sauce_tunnel.terminate()

            timeout = time.time()
            while sauce_tunnel.poll() is None:
                if time.time() - timeout < 30:
                    time.sleep(1)
                else:
                    sauce_tunnel.kill()

    readyfile = "."+tunnel_id
    sauce_tunnel = None
    try:
        sauce_log = file("sauce_tunnel.log", "w")
        sauce_tunnel = subprocess.Popen(
            ["java", "-jar", sauce_connect_local,
             "--readyfile", readyfile,
             "--tunnel-identifier", tunnel_id,
             sauce_username, sauce_access_key],
            stdout=sauce_log, stderr=sauce_log)

        atexit.register(kill_tunnel, sauce_tunnel)

        # Wait for the tunnel to come up
        while not os.path.exists(readyfile):
            time.sleep(0.5)

    except:
        if sauce_tunnel:
            kill_tunnel(sauce_tunnel)
        raise

    args.remote_executor = "http://%s:%s@localhost:4445/wd/hub" % (
        sauce_username, sauce_access_key)

    custom_data = {}
    git_info = subprocess.Popen(
        ["git", "describe", "--all", "--long"], stdout=subprocess.PIPE
    ).communicate()[0]
    custom_data["git-info"] = git_info

    git_commit = subprocess.Popen(
        ["git", "rev-parse", "HEAD"], stdout=subprocess.PIPE
    ).communicate()[0]
    custom_data["git-commit"] = git_commit

    caps['tags'] = []
    if 'TRAVIS_BUILD_NUMBER' in os.environ:
        # Send travis information upstream
        caps['build'] = "%s %s" % (
            os.environ['TRAVIS_REPO_SLUG'],
            os.environ['TRAVIS_BUILD_NUMBER'],
        )
        caps['name'] = "Travis run for %s" % os.environ['TRAVIS_REPO_SLUG']

        caps['tags'].append(
            "repo=%s" % os.environ['TRAVIS_REPO_SLUG'])
        caps['tags'].append(
            "branch=%s" % os.environ['TRAVIS_BRANCH'])

        travis_env = [
            'TRAVIS_BRANCH',
            'TRAVIS_BUILD_ID',
            'TRAVIS_BUILD_NUMBER',
            'TRAVIS_COMMIT',
            'TRAVIS_COMMIT_RANGE',
            'TRAVIS_JOB_ID',
            'TRAVIS_JOB_NUMBER',
            'TRAVIS_PULL_REQUEST',
            'TRAVIS_REPO_SLUG',
        ]

        for env in travis_env:
            tag = env[len('TRAVIS_'):].lower()
            value = os.environ.get(env, None)
            if not value:
                continue
            custom_data[tag] = value

        custom_data["github-url"] = (
            "https://github.com/%s/tree/%s" % (
                os.environ['TRAVIS_REPO_SLUG'], git_commit))
        custom_data["travis-url"] = (
            "https://travis-ci.org/%s/builds/%s" % (
                os.environ['TRAVIS_REPO_SLUG'],
                os.environ['TRAVIS_BUILD_ID']))
    else:
        # Collect information about who/what is running the test
        caps['name'] = "Manual run for %s" % getpass.getuser()
        caps['build'] = git_info

        caps['tags'].append('user=%s' % getpass.getuser())
        caps['tags'].append('host=%s' % socket.gethostname())

# -----------------------------------------------------------------------------

import subunit
import testtools

if args.list:
    data = file("test/testcases.js").read()
    for test in re.compile("(?<=').+(?=')").findall(data):
        print test[:-5]
    sys.exit(-1)

if args.load_list:
    tests = list(set(x.split(':')[0].strip()+'.html'
                 for x in args.load_list.readlines()))
else:
    tests = []

# Collect summary of all the individual test runs
summary = testtools.StreamSummary()

# Output information to stdout
if not args.subunit:
    # Output test failures
    result_streams = [testtools.TextTestResult(sys.stdout)]
    if args.verbose:
        import unittest
        # Output individual test progress
        result_streams.insert(0,
            unittest.TextTestResult(
                unittest.runner._WritelnDecorator(sys.stdout), False, 2))
    # Human readable test output
    pertest = testtools.StreamToExtendedDecorator(
        testtools.MultiTestResult(*result_streams))
else:
    from subunit.v2 import StreamResultToBytes
    pertest = StreamResultToBytes(sys.stdout)

    if args.list:
        output = subunit.CopyStreamResult([summary, pertest])
        output.startTestRun()
        for test in re.compile("(?<=').+(?=')").findall(
                file("test/testcases.js").read()):
            output.status(test_status='exists', test_id=test[:-5])

        output.stopTestRun()
        sys.exit(-1)

output = subunit.CopyStreamResult([summary, pertest])
output.startTestRun()

# Start up a local HTTP server which serves the files to the browser and
# collects the test results.
# -----------------------------------------------------------------------------
import SimpleHTTPServer
import SocketServer
import threading
import cgi
import re

import itertools
import mimetools
import mimetypes


class MultiPartForm(object):
    """Accumulate the data to be used when posting a form."""

    def __init__(self):
        self.form_fields = []
        self.files = []
        self.boundary = mimetools.choose_boundary()
        return

    def get_content_type(self):
        return 'multipart/form-data; boundary=%s' % self.boundary

    def add_field(self, name, value):
        """Add a simple field to the form data."""
        self.form_fields.append((name, value))
        return

    def add_file(self, fieldname, filename, fileHandle, mimetype=None):
        """Add a file to be uploaded."""
        body = fileHandle.read()
        if mimetype is None:
            mimetype = (
                mimetypes.guess_type(filename)[0] or
                'application/octet-stream')
        self.files.append((fieldname, filename, mimetype, body))
        return

    def __str__(self):
        """Return a string representing the form data, with attached files."""
        # Build a list of lists, each containing "lines" of the
        # request.  Each part is separated by a boundary string.
        # Once the list is built, return a string where each
        # line is separated by '\r\n'.
        parts = []
        part_boundary = '--' + self.boundary

        # Add the form fields
        parts.extend([
            part_boundary,
            'Content-Disposition: form-data; name="%s"' % name,
            '',
            value,
        ] for name, value in self.form_fields)

        # Add the files to upload
        parts.extend([
            part_boundary,
            'Content-Disposition: file; name="%s"; filename="%s"' % (
                field_name, filename),
            'Content-Type: %s' % content_type,
            '',
            body,
        ] for field_name, filename, content_type, body in self.files)

        # Flatten the list and add closing boundary marker,
        # then return CR+LF separated data
        flattened = list(str(b) for b in itertools.chain(*parts))
        flattened.append('--' + self.boundary + '--')
        flattened.append('')
        return '\r\n'.join(flattened)


critical_failure = False


class ServerHandler(SimpleHTTPServer.SimpleHTTPRequestHandler):
    STATUS = {0: 'success', 1: 'fail', 2: 'fail', 3: 'skip'}

    # Make the HTTP requests be quiet
    def log_message(self, format, *a):
        if args.verbose:
            SimpleHTTPServer.SimpleHTTPRequestHandler.log_message(
                self, format, *a)

    def do_POST(self):
        global critical_failure
        already_failed = critical_failure

        form = cgi.FieldStorage(
            fp=self.rfile,
            headers=self.headers,
            environ={
                'REQUEST_METHOD': 'POST',
                'CONTENT_TYPE': self.headers['Content-Type'],
            })

        overall_status = 0
        test_id = None
        try:
            json_data = form.getvalue('data')
            data = simplejson.loads(json_data)
        except ValueError, e:
            critical_failure = True

            test_id = "CRITICAL-FAILURE"

            msg = "Unable to decode JSON object (%s)\n%s" % (e, json_data)
            overall_status = 1
            output.status(
                test_id="CRITICAL-FAILURE",
                test_status='fail',
                test_tags=[args.browser],
                file_name='traceback',
                file_bytes=msg,
                mime_type='text/plain; charset=UTF-8',
                eof=True)
        else:
            test_id = data['testName'][:-5]
            for result in data['results']:
                info = dict(result)
                info.pop('_structured_clone', None)

                if not isinstance(result['message'], (str, unicode)):
                    msg = str(result['message'])
                else:
                    msg = result['message']

                overall_status += result['status']
                output.status(
                    test_id="%s:%s" % (test_id, result['name']),
                    test_status=self.STATUS[result['status']],
                    test_tags=[args.browser],
                    file_name='traceback',
                    file_bytes=msg,
                    mime_type='text/plain; charset=UTF-8',
                    eof=True)

            if args.verbose and 'debug' in data and overall_status > 0:
                output.status(
                    test_id="%s:debug-log" % (test_id),
                    test_status='fail',
                    test_tags=[args.browser],
                    file_name='traceback',
                    file_bytes=data['debug'],
                    mime_type='text/plain; charset=UTF-8',
                    eof=True)

        # Take a screenshot of result if a failure occurred.
        if overall_status > 0 and (args.virtual or args.browser == "Remote"):
            time.sleep(1)

            try:
                screenshot = test_id + '.png'
                if args.virtual:
                    disp.grab().save(screenshot)
                elif args.browser == "Remote":
                    global browser
                    browser.save_screenshot(screenshot)

                # On android we want to do a
                # adb run /system/bin/screencap -p /sdcard/FILENAME.png
                # adb cp FILENAME.png ....

                if args.upload and not already_failed:
                    form = MultiPartForm()
                    form.add_field('adult', 'no')
                    form.add_field('optsize', '0')
                    form.add_file(
                        'upload[]', screenshot, fileHandle=open(screenshot, 'rb'))

                    request = urllib2.Request("http://postimage.org/")
                    body = str(form)
                    request.add_header('Content-type', form.get_content_type())
                    request.add_header('Content-length', len(body))
                    request.add_data(body)

                    result = urllib2.urlopen(request).read()
                    print "Screenshot at:", re.findall("""<td><textarea wrap='off' onmouseover='this.focus\(\)' onfocus='this.select\(\)' id="code_1" scrolling="no">([^<]*)</textarea></td>""", result)  # noqa
            except Exception, e:
                print e

        response = "OK"
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.send_header("Content-length", len(response))
        self.end_headers()
        self.wfile.write(response)
        self.wfile.close()

if args.sauce:
    port = 55001
else:
    port = 0  # Bind to any port on localhost

while True:
    try:
        httpd = SocketServer.TCPServer(
            ("127.0.0.1", port),
            ServerHandler)
        break
    except socket.error as e:
        print e
        time.sleep(5)

port = httpd.socket.getsockname()[-1]
print "Serving at", port

httpd_thread = threading.Thread(target=httpd.serve_forever)
httpd_thread.daemon = True
httpd_thread.start()


# Start up a virtual display, useful for testing on headless servers.
# -----------------------------------------------------------------------------

VIRTUAL_SIZE = (1024, 2000)

# PhantomJS doesn't need a display
disp = None
if args.virtual and args.browser != "PhantomJS":
    from pyvirtualdisplay.smartdisplay import SmartDisplay

    try:
        disp = SmartDisplay(
            visible=0, bgcolor='black', size=VIRTUAL_SIZE).start()
        atexit.register(disp.stop)
    except:
        if disp:
            disp.stop()
        raise


# Start up the web browser and run the tests.
# ----------------------------------------------------------------------------

from selenium import webdriver
from selenium.common import exceptions as selenium_exceptions
from selenium.webdriver.common.keys import Keys as selenium_keys

driver_arguments = {}
if args.browser == "Chrome":
    import tempfile
    import shutil

    # We reference shutil to make sure it isn't garbaged collected before we
    # use it.
    def directory_cleanup(directory, shutil=shutil):
        try:
            shutil.rmtree(directory)
        except OSError, e:
            pass

    try:
        user_data_dir = tempfile.mkdtemp()
        atexit.register(directory_cleanup, user_data_dir)
    except:
        directory_cleanup(user_data_dir)
        raise

    driver_arguments['chrome_options'] = webdriver.ChromeOptions()
    # Make printable
    webdriver.ChromeOptions.__repr__ = lambda self: str(self.__dict__)
    chrome_flags = [
        '--user-data-dir=%s' % user_data_dir,
        '--enable-logging',
        '--start-maximized',
        '--disable-default-apps',
        '--disable-extensions',
        '--disable-plugins',
    ]
    chrome_flags += args.flag
    # Travis-CI uses OpenVZ containers which are incompatible with the sandbox
    # technology.
    # See https://code.google.com/p/chromium/issues/detail?id=31077 for more
    # information.
    if 'TRAVIS' in os.environ:
        chrome_flags += [
            '--no-sandbox',
            '--disable-setuid-sandbox',
            '--allow-sandbox-debugging',
        ]
    for flag in chrome_flags:
        driver_arguments['chrome_options'].add_argument(flag)

    #driver_arguments['chrome_options'].binary_location = (
    #    '/usr/bin/google-chrome')
    driver_arguments['executable_path'] = chromedriver


elif args.browser == "Firefox":
    driver_arguments['firefox_profile'] = webdriver.FirefoxProfile()
    # Firefox will often pop-up a dialog saying "script is taking too long" or
    # similar. So we can notice this problem we use "accept" rather then the
    # default "dismiss".
    webdriver.DesiredCapabilities.FIREFOX[
        "unexpectedAlertBehaviour"] = "accept"

elif args.browser == "PhantomJS":
    driver_arguments['executable_path'] = phantomjs
    driver_arguments['service_args'] = ['--remote-debugger-port=9000']

elif args.browser == "Remote":
    driver_arguments['command_executor'] = args.remote_executor

    for arg in args.remote_caps:
        if not arg.strip():
            continue

        if arg.find('=') < 0:
            caps.update(getattr(
                webdriver.DesiredCapabilities, arg.strip().upper()))
        else:
            bits = arg.split('=')
            base = caps
            for arg in bits[:-2]:
                if arg not in base:
                    base[arg] = {}
                base = base[arg]
            base[bits[-2]] = bits[-1]
    driver_arguments['desired_capabilities'] = caps

major_failure = False
browser = None
session_id = None
try:
    try:
        if args.verbose:
            print driver_arguments
        browser = getattr(webdriver, args.browser)(**driver_arguments)
        session_id = browser.session_id
        atexit.register(browser.quit)
    except:
        if browser:
            browser.quit()
        raise

    # Load an empty page so the body element is always visible
    browser.get('data:text/html;charset=utf-8,<!DOCTYPE html><html><body>EMPTY</body></html>')  # noqa
    if args.virtual and args.browser == "Firefox":
        # Calling browser.maximize_window() doesn't work as we don't have a
        # window manager, so instead we for the size/position.
        browser.set_window_position(0, 0)
        browser.set_window_size(*VIRTUAL_SIZE)
        # Also lets go into full screen mode to get rid of the "Chrome" around
        # the edges.
        e = browser.find_element_by_tag_name('body')
        e.send_keys(selenium_keys.F11)

    url = 'http://localhost:%i/test/test-runner.html?%s' % (
        port, "|".join(tests))
    browser.get(url)

    def close_other_windows(browser, url):
        for win in browser.window_handles:
            browser.switch_to_window(win)
            if browser.current_url != url:
                browser.close()
        browser.switch_to_window(browser.window_handles[0])

    while True:
        # Sometimes other windows are accidently opened (such as an extension
        # popup), close them.
        if len(browser.window_handles) > 1:
            close_other_windows(browser, url)

        try:
            v = browser.execute_script('return window.finished')
            if v:
                break

            try:
                progress = browser.execute_script('return window.getTestRunnerProgress()')
                status = '%s/%s (%s%%)' % (progress['completed'], progress['total'],
                    100 * progress['completed'] // progress['total'])
            except selenium_exceptions.WebDriverException, e:
                status = e

            print 'Running tests...', status
            sys.stdout.flush()
            time.sleep(1)

        # Deal with unexpected alerts, sometimes they are dismissed by
        # alternative means so we have to deal with that case too.
        except selenium_exceptions.UnexpectedAlertPresentException, e:
            try:
                alert = browser.switch_to_alert()
                sys.stderr.write("""\
WARNING: Unexpected alert found!
---------------------------------------------------------------------
%s
---------------------------------------------------------------------
""" % alert.text)
                alert.dismiss()
            except selenium_exceptions.NoAlertPresentException, e:
                sys.stderr.write(
                    "WARNING: Unexpected alert"
                    " which dissappeared on it's own!\n"
                )
            sys.stderr.flush()

except Exception, e:
    import traceback
    sys.stderr.write(traceback.format_exc())
    major_failure = True

finally:
    output.stopTestRun()

    if args.browser == "Chrome":
        log_path = os.path.join(user_data_dir, "chrome_debug.log")
        if os.path.exists(log_path):
            shutil.copy(log_path, ".")
        else:
            print "Unable to find Chrome log file:", log_path

if summary.testsRun == 0:
    print
    print "FAIL: No tests run!"

sys.stdout.flush()
sys.stderr.flush()

while args.dontexit and browser.window_handles:
    print "Waiting for you to close the browser...."
    sys.stdout.flush()
    sys.stderr.flush()
    time.sleep(1)

sys.stdout.flush()
sys.stderr.flush()

# Annotate the success / failure to sauce labs
if args.sauce and session_id:
    base64string = base64.encodestring(
        '%s:%s' % (sauce_username, sauce_access_key))[:-1]

    custom_data["failures"] = summary.failures
    custom_data["errors"] = summary.errors
    custom_data["tests"] = summary.testsRun
    custom_data["skipped"] = summary.skipped

    body_content = simplejson.dumps({
        "passed": summary.wasSuccessful(),
        "custom-data": custom_data,
    })
    connection = httplib.HTTPConnection("saucelabs.com")
    connection.request(
        'PUT', '/rest/v1/%s/jobs/%s' % (sauce_username, session_id),
        body_content,
        headers={"Authorization": "Basic %s" % base64string})
    result = connection.getresponse()
    print "Sauce labs updated:", result.status == 200

    import hmac
    from hashlib import md5
    key = hmac.new(
        str("%s:%s" % (sauce_username, session_id)),
        str(sauce_access_key),
        md5).hexdigest()
    url = "https://saucelabs.com/jobs/%s?auth=%s" % (
        browser.session_id, key)
    print "Sauce lab output at:", url

if summary.wasSuccessful() and summary.testsRun > 0 and not major_failure:
    sys.exit(0)
else:
    sys.exit(1)
