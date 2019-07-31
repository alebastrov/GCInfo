 ## This small app ets you embed short memory & GC information page. 
 It returns HTML like reflects actual status of memory and GC (see example below).
 
![Ashampoo_Snap_2019 07 30_10h18m45s_035_](https://user-images.githubusercontent.com/6348292/62216985-7e897900-b3b2-11e9-8938-3e447f354647.png)

Just put compiled files into /WEB-INF/classes and run
~~~
new GCInfo2HtmlPrinter().getHtml();
~~~

What we can see here?
Long horizontal bar reveals current memory usage - left (red) side - memory is in use, middle (white) is available memory without reallocation and GC, right (green) - reflects total amount of memory

For instance on image we can see 1 GiB memory total (-Xmx1g) 

There are bars on the image, each bar reflects GC job. The bar's height reflects how much memory GC freed, the bar's width reflects GC time taken for collecting, and color of the bar shows how % time GC took. The black bars on image reflects Stop-The-World or GC state near to it.


#### Note: first run will show an empty image, because it will start collecting GC events right that moment. You may like to start collecting with starting your application, in that case please put initializer nelow in your start-up class
~~~
GCInfoCollector.getGCInfoCollector(TimeUnit.SECONDS.toMillis(10));
~~~
10 seconds is the update time period, so if you take image often than 10 seconds, it may look stale.

Have fun!
