/*
  Copyright (C) 2016 Robert LaThanh
  Modifications Copyright(C) 2016 Fred Grott (GrottWorkShop)

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
package com.github.shareme.gwsfluidx.nonbinding.presenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.github.shareme.gwsfluidx.nonbinding.adaptable.AdaptableAdapter;
import com.github.shareme.gwsfluidx.nonbinding.adaptable.AdaptableViewModel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A rudimentary {@link PresenterBindingAdapter} that submits an item for
 * adapting when it comes into view.
 * Some of what makes it rudimentary are that: it will process duplicate
 * requests, it doesn't do predictive queuing (e.g., adapting for items below
 * the fold, likely to be scrolled onto the screen).
 *
 * Created by fgrott on 8/21/2016.
 */
@SuppressWarnings("unused")
public abstract class AdaptOnDemandPresenterBindingAdapter<VM,
        AVM extends AdaptableViewModel<VM> & ItemViewType,
        AA extends AdaptableAdapter<VM, AVM>,
        VH extends RecyclerView.ViewHolder,
        VHF extends ViewHolderFactory<VH>>
        extends PresenterBindingAdapter<VM, AVM, AA, VH, VHF> {

  //== Operating fields =======================================================

  private final @NonNull
  ExecutorService executorService;

  private  AsyncTask<Void, Void, VM> mTask = null;


  //== Constructors ===========================================================

  /**
   * The default executor service for adapting items uses a stack so the items
   * most recently scrolled into view are adapted first. Thus, items scrolled
   * into and then off of the screen before getting picked up for adapting are
   * adapted later. The number of threads it uses is up to double the number of
   * CPUs ({@code numCpus * 2 + 1}).
   */
  public AdaptOnDemandPresenterBindingAdapter(
          @NonNull LayoutInflater layoutInflater) {
    super(layoutInflater);

    // queue size must be at least as large as the number of items possibly on
    // the screen
    BlockingQueue<Runnable> queue = new LinkedBlockingDeque<Runnable>(128) {
      @Override
      public Runnable poll() {
        return super.pollLast();
      }
    };

    int cpus = Runtime.getRuntime().availableProcessors();
    this.executorService = new ThreadPoolExecutor(cpus + 1, cpus * 2 + 1,
            1, TimeUnit.SECONDS, queue);

  }

  /**
   * To fix to eliminate duplicate tasks, set an instance one to null
   * set the inner class to the instance and make sure to set
   * PostExecute and onCancelled to null again per this SO question and
   * answer:
   *
   * http://stackoverflow.com/questions/6645203/android%ADasynctask%ADavoid%ADmultiple%ADinstances%ADrunning5/5
   *
   * @param adaptableViewModel the {@link AVM AdaptableViewModel} that needs
   *        to be adapted.
   * @param position the position of the {@code adaptableViewModel}
   * @param presenter the presenter that can adapt this item
   */
  @Override
  protected void onAdaptNeeded(
          @NonNull final AVM adaptableViewModel, int position,
          @NonNull final Presenter<VM, AVM, AA, VH, VHF> presenter) {
    // item not yet adapted. start task to make this view available
    if (mTask == null) {
      mTask = new AsyncTask<Void, Void, VM>() {
        @Override
        protected VM doInBackground(Void... params) {
          // Naming the AsyncTask via Xion's answer to this SO question:
          // http://stackoverflow.com/questions/7585426/android%ADidentify%ADwhat%ADcode%ADan%ADasynctask%ADis%ADrunning2/2
          Thread.currentThread().setName("Presenter (AsyncTask)");
          if (adaptableViewModel.getViewModel() != null) {
            // another task must have beat me to adapting. (the item was
            // scrolled onto screen, I was created, the item was scrolled off
            // and back on, another duplicate task was added to the top of the
            // queue, that task finished, then I got executed.)
            return adaptableViewModel.getViewModel();
          }
          // this could still be a duplicate, concurrent job. oh well.
          return presenter.adaptableAdapter.adapt(adaptableViewModel);
        }

        @Override
        protected void onPostExecute(VM viewModel) {
          // save the adapted model to the adaptableViewModel so it's available
          // if it's requested again
          adaptableViewModel.setViewModel(viewModel);
          mTask = null;
        }

        @Override
        protected void onCancelled(){
          mTask = null;
        }
      };
      mTask.executeOnExecutor(executorService);
    }
  }

}
