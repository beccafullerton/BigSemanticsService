#!/usr/bin/python

import sys
import os
import csv
from os.path import join

from simple_config import load_config
from forker import fork, call, check_call

downloader_config = load_config("downloaders.conf")

class Downloaders:
  def __init__(self, config = None):
    if config is None:
      config = downloader_config
    self.config = config
    self.code_dir = config["code_dir"]
    self.dpool_dir = join(self.code_dir, "BigSemanticsService", "DownloaderPool")
    self.downloaders = config["downloaders"]

  def build(self):
    check_call(["ant", "clean"], wd = self.dpool_dir)
    check_call(["ant", "war"], wd = self.dpool_dir)
    check_call(["ant", "downloader-jar"], wd = self.dpool_dir)

  def put(self, local_file, remote_file):
    out_errs = []
    for d in self.downloaders:
      userhost = d.user + "@" + d.host
      out_errs.append(call(["scp", "-i", d.login_id, "-P", str(d.port),
                            local_file, userhost + ":" + remote_file]))
    return out_errs

  def get(self, remote_file, local_file):
    out_errs = []
    for d in self.downloaders:
      userhost = d.user + "@" + d.host
      out_errs.append(call(["scp", "-i", d.login_id, "-P", str(d.port),
                            userhost + ":" + remote_file, local_file]))
    return out_errs

  def do(self, cmd, use_fork = False):
    call_func = fork if use_fork else call
    out_errs = []
    for d in self.downloaders:
      userhost = d.user + "@" + d.host
      out_errs.append(
        call_func(["ssh", "-i", d.login_id, "-p", str(d.port), userhost, cmd]))
    return out_errs

  def update_jars(self):
    local_jar = join(self.dpool_dir, "build/Downloader.jar")
    remote_jar = "~/downloader/Downloader.jar"
    self.put(local_jar, remote_jar)

  def run_downloaders(self):
    run_cmd =\
      "nohup java -server -Xms128m -Xmx128m -jar ~/downloader/Downloader.jar >/dev/null 2>&1 &"
    self.do("killall java")
    self.do(run_cmd)



if __name__ == '__main__':
  w = sys.stderr.write
  if len(sys.argv) < 2:
    w("usages:\n")
    w("  {0} build: Build downloader jar\n".format(sys.argv[0]))
    w("  {0} update: Update downloader jars\n".format(sys.argv[0]))
    w("  {0} run: Run downloaders\n".format(sys.argv[0]))
    w("  {0} put <local_file> <remote_file>: Upload file\n".format(sys.argv[0]))
    w("  {0} get <remote_file> <local_file>: Download file\n".format(sys.argv[0]))
    w("  {0} do \"<command>\": Execute command\n".format(sys.argv[0]))
  else:
    ds = Downloaders()
    op = sys.argv[1]
    if op == 'build':
      ds.build()
    elif op == 'update':
      ds.update_jars()
    elif op == 'run':
      ds.run_downloaders()
    elif op == 'put' and len(sys.argv) == 4:
      for (code, out, err) in ds.put(sys.argv[2], sys.argv[3]):
        print "--\nReturn code: " + str(code)
        print "Output:\n" + out
        print "Error:\n" + out
    elif op == 'get' and len(sys.argv) == 4:
      for (code, out, err) in ds.get(sys.argv[2], sys.argv[3]):
        print "--\nReturn code: " + str(code)
        print "Output:\n" + out
        print "Error:\n" + out
    elif op == 'do' and len(sys.argv) == 3:
      for (code, out, err) in  ds.do(sys.argv[2]):
        print "--\nReturn code: " + str(code)
        print "Output:\n" + out
        print "Error:\n" + out
    else:
      w("unknown op: " + op)

