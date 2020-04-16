# JPV-math (backend)
JPV-math application serves to one purpose: modeling of shell's deformation state.

JPV-math backend consist of two services:
1. Java service. It's REST Java Spring service, which purpose is to manage data, which come from <a href="https://github.com/ereborDeveloper/math-modeling-gui">frontend</a> service.
2. Python service. It's flask support service providing expand of brackets and some difficult math parse-operations.

The idea of modeling of shell's deformation state is to find the minimun of functional.

To deploy this app you need:
1. Clone repository
2. Import project into IDEA as Maven project, enable auto-import and download all dependencies
3. Install Lombok plugin
4. Download Python and Java SDK (if you haven't it on your PC)
5. Configure projects in project structure (Ctrl + Alt + Shift + S). Create Python module and set Python interpreter.
6. Download all Python packages needed in _init_.py.
7. Run Java & Python main methods

Java uses port 9090, Python uses port 5000. You can test it by using Postman or any HTTP clients.

You can see unit tests to find out some examples. Backend provided with some basic logging in russian.
<img src="https://sun9-3.userapi.com/GNrNMLNPEY2sCLvrEgnJK6qqp0mGcc-vPUV-4A/XfQZCIOVKqc.jpg" />
