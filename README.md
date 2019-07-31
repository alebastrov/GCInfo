 ## This small app ets you embed short memory & GC information page. 
 It returns HTML like reflects actual status of memory and GC (see example below).
 
![Ashampoo_Snap_2019 07 30_10h18m45s_035_](https://user-images.githubusercontent.com/6348292/62216985-7e897900-b3b2-11e9-8938-3e447f354647.png)

Just put compiled files into /WEB-INF/classes and run
~~~
new GCInfo2HtmlPrinter().getHtml();
~~~

What we can see here?

There are bars on the image, each bar reflects GC job. The bar's height reflects how much memory GC freed, the bar's width reflects GC time taken for collecting, and color of the bar shows how % time GC took. The black bars on image reflects Stop-The-World or GC state near to it.


#### Note: first run will show an empty image, because it started collecting GC since you've requested it. You may like to start collecting with starting your application, in that case please use with your start-up class
~~~
GCInfoCollector.getGCInfoCollector(TimeUnit.SECONDS.toMillis(10));
~~~

Have fun!
