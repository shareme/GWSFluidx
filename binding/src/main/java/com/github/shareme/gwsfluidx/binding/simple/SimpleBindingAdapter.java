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
package com.github.shareme.gwsfluidx.binding.simple;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The SimpleBindingAdapter takes adaptable objects ("data model" objects that
 * aren't prepared for display) and adapts them to be displayed, and then binds
 * them to the View for rendering.
 * While adapting, which happens in the background, some sort of
 * progress indicator / loading placeholder should be displayed for unadapted
 * items instead.
 *
 * @param <A> the type of the object that will be adapted; "adaptable"
 * @param <VM> the type of the object that {@code <A>} will be adapted into;
 *        "View Model"
 * @param <VH> {@link RecyclerView.Adapter}'s {@code VH} type parameter
 *
 * Created by fgrott on 9/9/2016.
 */
@SuppressWarnings("unused")
public abstract class SimpleBindingAdapter<A, VM, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>  {
  //== Operating fields =======================================================

  protected List<AdaptableViewModel<A, VM>> items;


  //== Abstract methods =======================================================

  /**
   * Perform binding of the {@code ViewModel} to the View / (Loading)ViewHolder.
   *
   * @param viewModel The ViewModel for the item at {@code position}.
   *        This may be {@code null}.
   *        If it is, the implementation has already queued up the item for
   *        adapting in the background.
   */
  public abstract void onBindViewHolder(@NonNull final VH viewHolder,
                                        @NonNull VM viewModel,
                                        int position);

  /**
   * When a View Model is given to a View (via
   * {@link #onBindViewHolder(RecyclerView.ViewHolder, Object, int)})
   * it may be {@code null} (adapting has not yet completed).
   * Once the adapting has been completed, the View/ViewHolder may need to be
   * notified (if it hasn't already been recycled) so that it can be updated to
   * show the ViewModel.
   *
   * This is called when the ViewModel is ready and the viewHolder has not been
   * recycled.
   * Implementations using Android Data Binding will simply just have to provide
   * the {@code viewModel} to the {@code viewHolder}'s binder.
   */
  protected abstract void onViewModelReadyForViewHolder(
          @NonNull VH viewHolder, @NonNull VM viewModel);


  //== 'RecyclerView.Adapter' methods =========================================

  @Override
  public int getItemCount() {
    return items.size();
  }


  //== 'SimpleBindingAdapter' methods =========================================

  public void addAll(Collection<A> adaptables) {
    if (items == null) {
      items = new ArrayList<>(adaptables.size());
    }

    for (A adaptable : adaptables) {
      items.add(new AdaptableViewModel<A, VM>(adaptable));
    }
  }

}
