/*
  Copyright (C) 2016 Fred Grott(aka shareme GrottWorkShop)

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language
governing permissions and limitations under License.
 */
package com.github.shareme.gwsfluidx.binding.rvexts;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * An implementation of endless scrolling
 *
 * usage:
 *
 * <code>
 *   RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
 *   LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
 *   recyclerView.setLayoutManager(linearLayoutManager);
 *   recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
 *              @Override
 *              public void onLoadMore(int current_page) {
 *                  // do something...
 *               }
 *    });
 *
 * </code>
 * Created by fgrott on 9/14/2016.
 */
@SuppressWarnings("unused")
public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
  public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

  private int previousTotal = 0; // The total number of items in the dataset after the last load
  private boolean loading = true; // True if we are still waiting for the last set of data to load.
  private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
  int firstVisibleItem, visibleItemCount, totalItemCount;

  private int current_page = 1;

  private LinearLayoutManager mLinearLayoutManager;

  public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
    this.mLinearLayoutManager = linearLayoutManager;
  }

  @Override
  public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    super.onScrolled(recyclerView, dx, dy);

    visibleItemCount = recyclerView.getChildCount();
    totalItemCount = mLinearLayoutManager.getItemCount();
    firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

    if (loading) {
      if (totalItemCount > previousTotal) {
        loading = false;
        previousTotal = totalItemCount;
      }
    }
    if (!loading && (totalItemCount - visibleItemCount)
            <= (firstVisibleItem + visibleThreshold)) {
      // End has been reached

      // Do something
      current_page++;

      onLoadMore(current_page);

      loading = true;
    }
  }

  public abstract void onLoadMore(int current_page);
}