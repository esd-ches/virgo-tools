Building Virgo Tools
====================

The Virgo Tools are build with Maven/Tycho:

```
$ mvn clean verify
```

Setup local IDE for development
===============================

Download Eclipse IDE "Eclipse IDE for Java EE Developers".

Add SWTBot [https://wiki.eclipse.org/SWTBot] (Update site: http://download.eclipse.org/technology/swtbot/releases/latest/)

If you are a committer clone the Virgo Tooling from repository

```
$ git clone ssh://<commiterid>@git.eclipse.org/gitroot/virgo/org.eclipse.virgo.ide.git
```

or use the anonymous URL otherwise:

```
$ git clone git://git.eclipse.org/gitroot/virgo/org.eclipse.virgo.ide.git
```

Use the Eclipse command "Import existing Maven project", select the root folder of the git repository and import the projects into your IDE.

Set `MarsTargetPlatform.target` as target definition and you should be ready to go.

 If you have any trouble following this instructions please do not hesitate to contact the Virgo developers via the Virgo Project Mailinglist: <virgo-dev@eclipse.org>.
